package com.example.data.apk

import android.util.Base64
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.cms.CMSSignedDataGenerator
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ApkSignerHelper {

    // Pre-generated 2048-bit RSA TestKey & X509 Certificate for signing lightweight APKs
    private const val DEFAULT_PRIVATE_KEY_B64 =
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCruW7np9nWetgY0BoX2CH580B3" +
        "HwwlLDC3GpfkRxsZlkpqgZeIvj7ORP2IrQWCr1tjia9WjdIij+uVsn42BT/PBRmRlTOwP+Cfsk6a" +
        "ZgaH1XcpYWNqRRWfUbn9HKF+UXBIJ21bndBomvMujoQBmeRzTIPst4n6u/A9qdTpuOWQuEhsf7O3" +
        "o+O/QBrOK5h/uidHzLBAC0W53ON+CYyQHUL4sbWpBAP2D3G49ttXy1UD/4yhUzlZatRdW/5/uW2e" +
        "XDQfE+/moOKAFp2snVFyWW+7CUQjR44laLP+q16COnVBkUBxyh3jglDia/6ykPndlTTYEWEP8uZg" +
        "S3Omm1MsY641AgMBAAECggEAKHZO7YKdT24ukNon68pfZxer7uV/5RpeeCZp973RYtwIuMPEQtIg" +
        "1lVnL9ck30AWVoPQrqJICyDCL8mY932RJWRcO/Nd9H94m+hVoQe9LjBcHYH2/Mk+aQXGvx8J5kum" +
        "Gzo/CEFNI/iuqB+odwAd1hUHWLV+a3fxCXaE1nVckhaWyU6lKS6Pq0bPi10MQ+GrNRxfy08O2Q84" +
        "H+JVaqrQyh/Kb3x8DTNxuwR+dJDEJ/wdiTM/q0/oAJqtV79nH+TjZ5XhGHanJxZL43h6UpFwTIEq" +
        "UpEx/PT9IWEDlDYTHCgFGQZWZ1mlEhw1joIdWWsEv/Ou5BL5JnoehpZ+kl9fAQKBgQDBcy7RB1sc" +
        "9/Pl6E/C+gQUer3Uo3cW4LaWM0e3+LEcxm2BJYXxUP3RIL8fzfkmn3uTxpRr19elwNs6pJWsGvWH" +
        "4QeH/3uAYxC2CjIFf+a2fi1HHaoAwb/YwfaPRtUFmchV5M91R9wE4YJ6S+vtffMiUNsZeDXHrOnX" +
        "zAILmW7FVQKBgQDjP+pKxOmBFisrWJHeU5R30L7NsPSyzvgx63VRNaP+U62qh2w+DTijCl8IFCmA" +
        "xg6Sz4aFpafzBV180/+q2JRXOMFcn8O+hjy6racuMEFn33//vsSzYkizm/ZHAWlcH1xobY5ppjnE" +
        "7cJ/amWW6JAIVUV8BFP1/m0Y8OVjJU9FYQKBgQCB7YvHJuqiNUMh6nsP2H4/BwVSuQu2WO0pSn3j" +
        "6WuDiR5pPDuPDbe76wOTJ/MsZrdIVHrBR/H1yOc1pu9D7cN1JMW7KPPZEjBaI9Te7r2VWn+soRba" +
        "dUcWHYgtSQSwQ05Tql9QRRhYSuIoo69tDkfrh2Tw4VU1rERPBR1mjwHgFQKBgCEY6P20yjFz0hvB" +
        "DZWs5J3CuFdq42i7fih/G5oVTuo2s793c8thz5LnasnZbeYEcpDtSrFiXCCn8mLA2aa/XnOJn5fm" +
        "6PJxyJyDuCqg+pWvVowf87QQp3gJSggza23wX1wSQMCgZh+JZV97VopxGrEsAi+6zaOxR6BvTWlw" +
        "QKhhAoGAbz9RoAZoOfv10cqTBky9c9xsu1RLQP3ncc6FdVWRvLPeYyFfnKwUOZzP38uT7GormWnG" +
        "w6qb4rx/40rozwlLvcWbN+Mu/tBfuvKkvxqakanGNsU9raaFiyw2X3ztx2Apb/FQAVsI/0oS/umb" +
        "PxoluCzd83qJHHuaRyXqOnFmrHY="

    private const val DEFAULT_CERTIFICATE_B64 =
        "MIIDfDCCAmSgAwIBAgIJAK2VuFFBhUG6MA0GCSqGSIb3DQEBDAUAMGsxCzAJBgNVBAYTAlVTMRAw" +
        "DgYDVQQIEwdVbmtub3duMRAwDgYDVQQHEwdVbmtub3duMRIwEAYDVQQKEwlIVE1MIExpdmUxEDAOB" +
        "gNVBAsTB0J1aWxkZXIxEjAQBgNVBAMTCUhUTUwgTGl2ZTAgFw0yNjA5MjgxNTQwNDBaGA8yMDU0" +
        "MDIxMzE1NDA0MFowazELMAkGA1UEBhMCVVMxEDAOBgNVBAgTB1Vua25vd24xEDAOBgNVBAcTB1Vu" +
        "a25vd24xEjAQBgNVBAoTCUhUTUwgTGl2ZTEQMA4GA1UECxMHQnVpbGRlcjESMBAGA1UEAxMJSFRN" +
        "TCBMaXZlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq7lu56fZ1nrYGNAaF9gh+fNA" +
        "dx8MJSwwtxqX5EcbGZZKaoGXiL4+zkT9iK0Fgq9bY4mvVo3SIo/rlbJ+NgU/zwUZkZUzsD/gn7JO" +
        "mmYGh9V3KWFjakUVn1G5/RyhflFwSCdtW53QaJrzLo6EAZnkc0yD7LeJ+rvwPanU6bjlkLhIbH+z" +
        "t6Pjv0AaziuYf7onR8ywQAtFudzjfgmMkB1C+LG1qQQD9g9xuPbbV8tVA/+MoVM5WWrUXVv+f7lt" +
        "nlw0HxPv5qDigBadrJ1RcllvuwlEI0eOJWiz/qtegjp1QZFAccod44JQ4mv+spD53ZU02BFhD/Lm" +
        "YEtzpptTLGOuNQIDAQABoyEwHzAdBgNVHQ4EFgQUpSXpb6M5ZCLHkowcth9eL7y8o/YwDQYJKoZI" +
        "hvcNAQEMBQADggEBACJg2lyI6994TYrDnJrUJelqsFvFSOe3G+Ph2CQxCoa77Yxk0QX64Ne2k1zi" +
        "d74ZbWMyrYxr9N5/gxbiOPtmzAQuk6lVuryy3/VGHmzYG47NrRWolO64478jUjG/7ex5Ak84o7P5" +
        "oOtsZWzB8QKx7unEYbeSKkR7xyHGBdkp0R3/LQSm1fnMRPA1iiosqVLqY87lXImZ3qYXRsFAfOQk" +
        "DgJp+OW4I8hKb0OHcX7IZWCYmwswvhCTRspEV3NNF0AiTjg/XxOFAMU5JRuc92IAh4Kpc8nKcjur" +
        "EEJlgnUVF42ssXVN5Ph5TVRIFDRfhsOnW2up+Ala3xqi5XTOCDipaMA="

    fun signApk(unsignedApk: File, signedApk: File) {
        val privateKey = loadPrivateKey(DEFAULT_PRIVATE_KEY_B64)
        val certificate = loadCertificate(DEFAULT_CERTIFICATE_B64)

        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(FileInputStream(unsignedApk)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && !entry.name.startsWith("META-INF/")) {
                    entries[entry.name] = zis.readBytes()
                }
                entry = zis.nextEntry
            }
        }

        // 1. Generate MANIFEST.MF
        val md = MessageDigest.getInstance("SHA-256")
        val manifestMf = StringBuilder()
        manifestMf.append("Manifest-Version: 1.0\r\n")
        manifestMf.append("Created-By: 1.0 (HTML Live APK Builder)\r\n\r\n")

        val entryManifestLines = mutableMapOf<String, String>()

        for ((name, bytes) in entries) {
            val digest = Base64.encodeToString(md.digest(bytes), Base64.NO_WRAP)
            val chunk = "Name: $name\r\nSHA-256-Digest: $digest\r\n\r\n"
            entryManifestLines[name] = chunk
            manifestMf.append(chunk)
        }
        val manifestMfBytes = manifestMf.toString().toByteArray(Charsets.UTF_8)

        // 2. Generate CERT.SF
        val manifestDigest = Base64.encodeToString(md.digest(manifestMfBytes), Base64.NO_WRAP)
        val certSf = StringBuilder()
        certSf.append("Signature-Version: 1.0\r\n")
        certSf.append("Created-By: 1.0 (HTML Live APK Builder)\r\n")
        certSf.append("SHA-256-Digest-Manifest: $manifestDigest\r\n\r\n")

        for ((name, _) in entries) {
            val chunk = entryManifestLines[name]!!
            val chunkDigest = Base64.encodeToString(md.digest(chunk.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
            certSf.append("Name: $name\r\n")
            certSf.append("SHA-256-Digest: $chunkDigest\r\n\r\n")
        }
        val certSfBytes = certSf.toString().toByteArray(Charsets.UTF_8)

        // 3. Generate CERT.RSA using PKCS#7 / CMS
        val contentSigner = JcaContentSignerBuilder("SHA256withRSA").build(privateKey)
        val certHolder = JcaX509CertificateHolder(certificate)
        val gen = CMSSignedDataGenerator()
        gen.addSignerInfoGenerator(
            JcaSignerInfoGeneratorBuilder(JcaDigestCalculatorProviderBuilder().build())
                .build(contentSigner, certHolder)
        )
        gen.addCertificate(certHolder)
        val cms = gen.generate(CMSProcessableByteArray(certSfBytes), true)
        val certRsaBytes = cms.encoded

        // 4. Output signed APK
        if (signedApk.exists()) signedApk.delete()
        ZipOutputStream(FileOutputStream(signedApk)).use { zos ->
            // Write existing entries
            for ((name, bytes) in entries) {
                val ze = ZipEntry(name)
                zos.putNextEntry(ze)
                zos.write(bytes)
                zos.closeEntry()
            }

            // Write META-INF signature files
            val manifestEntry = ZipEntry("META-INF/MANIFEST.MF")
            zos.putNextEntry(manifestEntry)
            zos.write(manifestMfBytes)
            zos.closeEntry()

            val certSfEntry = ZipEntry("META-INF/CERT.SF")
            zos.putNextEntry(certSfEntry)
            zos.write(certSfBytes)
            zos.closeEntry()

            val certRsaEntry = ZipEntry("META-INF/CERT.RSA")
            zos.putNextEntry(certRsaEntry)
            zos.write(certRsaBytes)
            zos.closeEntry()
        }
    }

    private fun loadPrivateKey(base64: String): PrivateKey {
        val decoded = Base64.decode(base64, Base64.DEFAULT)
        val spec = PKCS8EncodedKeySpec(decoded)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(spec)
    }

    private fun loadCertificate(base64: String): X509Certificate {
        val decoded = Base64.decode(base64, Base64.DEFAULT)
        val cf = CertificateFactory.getInstance("X.509")
        return cf.generateCertificate(ByteArrayInputStream(decoded)) as X509Certificate
    }
}
