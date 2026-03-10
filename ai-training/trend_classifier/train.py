from __future__ import annotations

from pathlib import Path

import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from torchvision import datasets, transforms

from common.training_utils import ensure_output_dir


class TrendClassifier(nn.Module):
    def __init__(self, num_classes: int = 3) -> None:
        super().__init__()
        self.net = nn.Sequential(
            nn.Conv2d(3, 32, 3, padding=1),
            nn.ReLU(),
            nn.MaxPool2d(2),
            nn.Conv2d(32, 64, 3, padding=1),
            nn.ReLU(),
            nn.MaxPool2d(2),
            nn.Conv2d(64, 128, 3, padding=1),
            nn.ReLU(),
            nn.AdaptiveAvgPool2d((1, 1)),
            nn.Flatten(),
            nn.Linear(128, num_classes),
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        return self.net(x)


def train(
    dataset_root: str = "../../dataset/trend_classifier",
    epochs: int = 25,
    batch_size: int = 32,
    lr: float = 1e-3,
) -> None:
    transform = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.ToTensor(),
    ])
    dataset = datasets.ImageFolder(dataset_root, transform=transform)
    loader = DataLoader(dataset, batch_size=batch_size, shuffle=True, num_workers=4)

    model = TrendClassifier(num_classes=len(dataset.classes))
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model.to(device)

    optimizer = torch.optim.Adam(model.parameters(), lr=lr)
    criterion = nn.CrossEntropyLoss()

    model.train()
    for epoch in range(epochs):
        epoch_loss = 0.0
        for images, labels in loader:
            images = images.to(device)
            labels = labels.to(device)

            optimizer.zero_grad()
            logits = model(images)
            loss = criterion(logits, labels)
            loss.backward()
            optimizer.step()

            epoch_loss += float(loss.item())

        avg_loss = epoch_loss / max(1, len(loader))
        print(f"epoch={epoch + 1}/{epochs} loss={avg_loss:.4f}")

    out = ensure_output_dir("trend_classifier")
    pt_path = out / "trend_classifier.pt"
    torch.save(model.state_dict(), pt_path)

    model.eval()
    sample = torch.randn(1, 3, 224, 224).to(device)
    onnx_path = out / "trend_classifier.onnx"
    torch.onnx.export(
        model,
        sample,
        onnx_path,
        input_names=["image"],
        output_names=["trend_logits"],
        dynamic_axes={"image": {0: "batch"}, "trend_logits": {0: "batch"}},
        opset_version=17,
    )

    classes_path = out / "classes.txt"
    classes_path.write_text("\n".join(dataset.classes))


if __name__ == "__main__":
    train()
