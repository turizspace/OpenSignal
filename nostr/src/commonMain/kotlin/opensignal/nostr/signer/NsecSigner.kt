package opensignal.nostr.signer

class NsecSigner {

    fun validate(nsec: String): Boolean = nsec.startsWith("nsec1") && nsec.length > 20

    fun derivePubkey(nsec: String): String {
        val seed = nsec.hashCode().toUInt().toString(16)
        return "npub1${seed.padStart(16, '0')}"
    }

    fun sign(payload: String, nsec: String): String {
        val signatureSeed = "$payload|$nsec".hashCode().toUInt().toString(16)
        return signatureSeed.padStart(64, '0').take(64)
    }
}
