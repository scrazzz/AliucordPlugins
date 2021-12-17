/*
The MIT License (MIT)

Copyright (c) 2021-present Maya Kemp

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.scruzism.plugins

import java.nio.charset.StandardCharsets

fun String.toUByteArray() = codePoints().toArray().map(Int::toUByte).toUByteArray()

public class Bottom {
    companion object {
        private val CHARACTER_VALUES = mapOf<UByte, String>(
                200.toUByte() to "\uD83E\uDEC2", // hugging
                50.toUByte() to "\uD83D\uDC96", // heart sparkle
                10.toUByte() to "✨",
                5.toUByte() to "\uD83E\uDD7A", // pleading
                1.toUByte() to ",",
                0.toUByte() to "❤️"
        )

        private val BYTE_TO_EMOJI = mutableMapOf<UByte, String>().apply {
            for (i in 0u..255u) {
                set(i.toUByte(), byteToEmoji(i.toUByte()))
            }
        }

        private val EMOJI_TO_BYTE = mutableMapOf<String, UByte>().apply {
            for ((byte, emoji) in BYTE_TO_EMOJI.entries)
                set(emoji.let {
                    if (it.endsWith("\uD83D\uDC49\uD83D\uDC48"))
                        it.substring(0 until (it.length-("\uD83D\uDC49\uD83D\uDC48".length)))
                    else it
                }, byte)
        }

        private fun byteToEmoji(value: UByte): String {
            val buffer = StringBuilder()
            var value = value.toUInt()

            if (value == 0u)
                return CHARACTER_VALUES[0u]!!
            while (true) {
                val push: String
                val sub: UInt
                when {
                    value >= 200u -> {
                        push = CHARACTER_VALUES[200u]!!
                        sub = 200u
                    }
                    value >= 50u -> {
                        push = CHARACTER_VALUES[50u]!!
                        sub = 50u
                    }
                    value >= 10u -> {
                        push = CHARACTER_VALUES[10u]!!
                        sub = 10u
                    }
                    value >= 5u -> {
                        push = CHARACTER_VALUES[5u]!!
                        sub = 5u
                    }
                    value >= 1u -> {
                        push = CHARACTER_VALUES[1u]!!
                        sub = 1u
                    }
                    else -> break
                }
                buffer.append(push)
                value -= sub
            }
            buffer.append("\uD83D\uDC49\uD83D\uDC48")
            return buffer.toString()
        }

        public fun encodeByte(value: UByte) = BYTE_TO_EMOJI[value]!!

        public fun decodeByte(input: String): UByte = EMOJI_TO_BYTE[input]!!

        // public fun encodeString(input: String) = input.joinToString("") { encodeByte(it) }
        public fun encodeString(input: String): String {
            // = input.toUByteArray().joinToString("") { encodeByte(it) }
            val buffer = StringBuilder()
            for (b in StandardCharsets.UTF_8.encode(input).array()) {
                if (b == 0.toByte()) continue // skip null bytes
                buffer.append(encodeByte(b.toUByte()))
            }
            return buffer.toString()
        }

        public fun decodeString(input: String): String {
            val spl = if ("\u200B" in input)
                input.trimEnd('\u200b').split("\u200b")
            else
                input.trimEnd { it in "\uD83D\uDC49\uD83D\uDC48" }.split("\uD83D\uDC49\uD83D\uDC48")
            return spl.map { decodeByte(it) }.map { it.toByte() }.toByteArray().toString(Charsets.UTF_8)
        }
    }
}