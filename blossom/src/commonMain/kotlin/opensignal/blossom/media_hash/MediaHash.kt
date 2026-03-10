package opensignal.blossom.media_hash

object MediaHash {

    // Non-cryptographic fallback hash suitable for deterministic IDs in offline scaffolds.
    fun pseudoSha256(bytes: ByteArray): String {
        var hash = 0xcbf29ce484222325UL
        val prime = 0x100000001b3UL
        bytes.forEach { byte ->
            hash = hash xor byte.toUByte().toULong()
            hash *= prime
        }

        val hex = hash.toString(16).padStart(16, '0')
        return (hex.repeat(4)).take(64)
    }
}
