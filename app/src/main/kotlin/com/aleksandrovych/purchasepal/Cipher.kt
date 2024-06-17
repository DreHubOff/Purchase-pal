package com.aleksandrovych.purchasepal

object Cipher {

    private const val key = '>'

    fun encryptDecryptXOR(input: String): String {
        val result = StringBuilder()
        for (element in input.toCharArray()) {
            result.append((element.code xor key.code).toChar())
        }
        return result.toString()
    }
}