package org.kin.framework.utils;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2024/5/25
 */
public final class CertUtils {
    /** 算法 */
    private static final String ALGORITHM = "SHA256withRSA";

    /** 证书key */
    public static final String CA_CERT = "ca_cert";
    /** server密钥key */
    public static final String SERVER_KEY = "server_key";
    /** server证书key */
    public static final String SERVER_CERT = "server_cert";
    /** client密钥key */
    public static final String CLIENT_KEY = "client_key";
    /** client证书key */
    public static final String CLIENT_CERT = "client_cert";

    private CertUtils() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 自己作为CA, 颁布根证书
     *
     * @param caSub     caSub
     * @param startDate 有效开始时间
     * @param endDate   有效结束时间
     * @return 自签名私钥
     */
    private static KeyStore.PrivateKeyEntry genSelfSignedCert(String caSub,
                                                              Date startDate,
                                                              Date endDate) {
        try {
            KeyPair keyPair = genKeyPair();
            //公钥
            PublicKey publicKey = keyPair.getPublic();
            //私钥
            PrivateKey privateKey = keyPair.getPrivate();

            //待签名公钥
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
            //issuer与 subject相同
            X500Name x500Name = new X500Name(caSub);
            X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(x500Name,
                    BigInteger.valueOf(System.currentTimeMillis()), startDate, endDate, x500Name, publicKeyInfo);

            //签名算法
            AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(ALGORITHM);
            //hash算法
            AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
            //使用自己的私钥进行签名
            //非对称加密参数
            AsymmetricKeyParameter asymmetricKeyParameter = PrivateKeyFactory.createKey(privateKey.getEncoded());
            ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(asymmetricKeyParameter);

            BcX509ExtensionUtils extUtils = new BcX509ExtensionUtils();
            certificateBuilder.addExtension(org.bouncycastle.asn1.x509.Extension.subjectKeyIdentifier, false,
                    extUtils.createSubjectKeyIdentifier(publicKeyInfo));
            certificateBuilder.addExtension(org.bouncycastle.asn1.x509.Extension.authorityKeyIdentifier, false,
                    extUtils.createAuthorityKeyIdentifier(publicKeyInfo));
            //CA证书
            certificateBuilder.addExtension(org.bouncycastle.asn1.x509.Extension.basicConstraints, false,
                    new BasicConstraints(true));

            //生成证书
            X509CertificateHolder holder = certificateBuilder.build(sigGen);
            X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(holder);

            return new KeyStore.PrivateKeyEntry(privateKey, new Certificate[]{certificate});
        } catch (Exception e) {
            throw new CertGenerateException(e);
        }
    }

    /**
     * 生成密钥对
     * 1024位
     */
    private static KeyPair genKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        return kpg.generateKeyPair();
    }

    /**
     * 生成证书
     *
     * @param caPrivateKey ca密钥
     * @param caSub        caSub
     * @param startDate    有效开始时间
     * @param endDate      有效结束时间
     * @return 私钥
     */
    private static KeyStore.PrivateKeyEntry genCert(PrivateKey caPrivateKey,
                                                    String caSub,
                                                    String sub,
                                                    Date startDate,
                                                    Date endDate) {
        try {
            //创建CSR证书
            KeyPair keyPair = genKeyPair();
            //公钥
            PublicKey publicKey = keyPair.getPublic();
            //私钥
            PrivateKey privateKey = keyPair.getPrivate();

            //待签名的subject
            PKCS10CertificationRequestBuilder certificationRequestBuilder = new PKCS10CertificationRequestBuilder(new X500Name(sub),
                    SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
            ContentSigner contentSigner = new JcaContentSignerBuilder(ALGORITHM).build(privateKey);
            PKCS10CertificationRequest certificationRequest = certificationRequestBuilder.build(contentSigner);
            byte[] csr = certificationRequest.getEncoded();

            PKCS10CertificationRequest p10CSR = new PKCS10CertificationRequest(csr);
            SubjectPublicKeyInfo pkInfo = p10CSR.getSubjectPublicKeyInfo();
            RSAKeyParameters rsaParams = (RSAKeyParameters) PublicKeyFactory.createKey(pkInfo);
            RSAPublicKeySpec rsaSpec = new RSAPublicKeySpec(rsaParams.getModulus(), rsaParams.getExponent());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey rsaPub = kf.generatePublic(rsaSpec);

            //待签名的公钥
            SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(rsaPub.getEncoded());
            X509v1CertificateBuilder certificateBuilder = new X509v1CertificateBuilder(
                    new X500Name(caSub), BigInteger.ONE, startDate, endDate, p10CSR.getSubject(), keyInfo);
            //签名算法
            AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(ALGORITHM);
            //哈希算法
            AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
            //CA的私钥
            AsymmetricKeyParameter asymmetricKeyParameter = PrivateKeyFactory.createKey(caPrivateKey.getEncoded());
            ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(asymmetricKeyParameter);

            //生成的证书是被CA签名的证书
            X509CertificateHolder certificateHolder = certificateBuilder.build(sigGen);
            X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certificateHolder);

            return new KeyStore.PrivateKeyEntry(privateKey, new Certificate[]{certificate});
        } catch (Exception e) {
            throw new CertGenerateException(e);
        }
    }

    /**
     * 生成密钥和证书
     *
     * @param caSub     caSub
     * @param serverSub serverSub
     * @param clientSub clientSub
     * @param startDate 有效开始时间
     * @param endDate   有效结束时间
     * @return 证书信息
     */
    public static Map<String, Object> genCertAndKeys(String caSub,
                                                     String serverSub,
                                                     String clientSub,
                                                     Date startDate,
                                                     Date endDate) {
        Map<String, Object> result = new HashMap<>();

        KeyStore.PrivateKeyEntry caKeyAndCert = genSelfSignedCert(caSub, startDate, endDate);
        PrivateKey privateKey = caKeyAndCert.getPrivateKey();

        // 用CA给server签发证书
        KeyStore.PrivateKeyEntry serverKeyAndCert = genCert(privateKey, caSub, serverSub, startDate, endDate);
        // 用CA给client签发证书
        KeyStore.PrivateKeyEntry clientKeyAndCert = genCert(privateKey, caSub, clientSub, startDate, endDate);

        result.put(CA_CERT, caKeyAndCert.getCertificate());
        result.put(SERVER_KEY, serverKeyAndCert.getPrivateKey());
        result.put(SERVER_CERT, serverKeyAndCert.getCertificate());
        result.put(CLIENT_KEY, clientKeyAndCert.getPrivateKey());
        result.put(CLIENT_CERT, clientKeyAndCert.getCertificate());
        return result;
    }
}
