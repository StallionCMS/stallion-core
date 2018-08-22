/*
 * Stallion Core: A Modern Web Framework
 *
 * Copyright (C) 2015 - 2016 Stallion Software LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
 * License for more details. You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-2.0.html>.
 *
 *
 *
 */

package io.stallion.utils;

import io.stallion.exceptions.DecryptionException;
import io.stallion.services.Log;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * A simple class for encrypting and decrypting an arbitrary bit of text
 *
 * - Uses AES/GCM mode
 * - Accepts a password, will generate a salt, derive a key using PBKDF2WithHmacSHA1, and append the salt to the output
 * - returns the encrypted text in base32 format
 * - Uses 128 big key length
 *
 * I would have greatly preferred just to use the Spring Crypto Library, but unfortunately that uses 256-bit keys,
 * which means the Ulimited Crypto libraries need to be installed, which makes using Stallion out of the box more
 * painful.
 *
 *
 *
 */
public class Encrypter {
    private static final int ITERATIONS = 1024;
    private static final int KEY_LENGTH = 128; // bits

    /*
    public static String encryptString(String password, String salt, String value) {
        StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
        textEncryptor.setPassword(password);
        String myEncryptedText = textEncryptor.encrypt(value);
        return myEncryptedText;
        //return Encryptors.text(password, salt).encrypt(value);
    }

    public static String decryptString(String password, String salt, String encrypted) {
        StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
        textEncryptor.setPassword(password);
        String text = textEncryptor.decrypt(encrypted);
        return text;
    }
   */

    public static String encryptString(String password, String value) {
        String salt = KeyGenerators.string().generateKey();
        SecretKeySpec skeySpec = makeKeySpec(password, salt);
        byte[] iv = KeyGenerators.secureRandom(16).generateKey();
        String ivString = Hex.encodeHexString(iv);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec,
                    new GCMParameterSpec(128, iv));
                    /*
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec,
                    new IvParameterSpec(iv));
                    */

            byte[] encrypted = cipher.doFinal(value.getBytes(Charset.forName("UTF-8")));
            String s = StringUtils.strip(new Base32().encodeAsString(encrypted), "=").toLowerCase();
            // Strip line breaks
            s = salt + ivString + s.replaceAll("(\\n|\\r)", "");
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decryptString(String password, String encryptedBase32) {
        try {
            return doDecryptString(password, encryptedBase32);
        } catch (Exception e) {
            Log.exception(e, "Exception trying to decrypt token");
            throw new DecryptionException("Error trying to decrypt the token.");
        }
    }

    private static String doDecryptString(String password, String encryptedBase32) throws Exception {
        encryptedBase32 = StringUtils.strip(encryptedBase32, "=");
        String salt = encryptedBase32.substring(0, 16);
        String ivString = encryptedBase32.substring(16, 48);
        byte[] iv = new byte[0];
        try {
            iv = Hex.decodeHex(ivString.toCharArray());
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
        encryptedBase32 = encryptedBase32.substring(48);
        Base32 decoder = new Base32();
        byte[] encrypted = decoder.decode(encryptedBase32.toUpperCase());
        SecretKeySpec skeySpec = makeKeySpec(password, salt);



            /*
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec,
                    new IvParameterSpec(iv));
              */
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec,
                new GCMParameterSpec(128, iv));

        byte[] original = cipher.doFinal(encrypted);
        return new String(original, Charset.forName("UTF-8"));

    }

    private static SecretKeySpec makeKeySpec(String password, String salt) {
        byte[] saltBytes = new byte[0];
        try {
            saltBytes = Hex.decodeHex(salt.toCharArray());
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), saltBytes,
                ITERATIONS, KEY_LENGTH);
        SecretKey secretKey;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            secretKey = factory.generateSecret(keySpec);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Not a valid encryption algorithm", e);
        }
        catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Not a valid secret key", e);
        }
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
        return skeySpec;
    }
}
