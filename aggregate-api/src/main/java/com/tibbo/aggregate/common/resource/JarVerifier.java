package com.tibbo.aggregate.common.resource;

import java.io.*;
import java.security.cert.*;
import java.util.*;
import java.util.jar.*;

public class JarVerifier
{
  
  public static void verify(JarEntry je, X509Certificate[] trustedCaCerts) throws IOException, CertificateException
  {
    if (je.isDirectory())
    {
      return;
    }
    
    // Every file must be signed - except
    // files in META-INF
    Certificate[] certs = je.getCertificates();
    if ((certs == null) || (certs.length == 0))
    {
      if (!je.getName().startsWith("META-INF"))
        throw new SecurityException("Unsigned entry: " + je.getName());
    }
    else
    {
      // Check whether the file
      // is signed as expected.
      // The framework may be signed by
      // multiple signers. At least one of
      // the signers must be a trusted signer.
      
      // First, determine the roots of the certificate chains
      X509Certificate[] chainRoots = getChainRoots(certs);
      boolean signedAsExpected = false;
      
      for (int i = 0; i < chainRoots.length; i++)
      {
        if (isTrusted(chainRoots[i], trustedCaCerts))
        {
          signedAsExpected = true;
          break;
        }
      }
      
      if (!signedAsExpected)
      {
        throw new SecurityException("The JAR is not signed by a trusted signer");
      }
    }
  }
  
  public static boolean isTrusted(X509Certificate cert, X509Certificate[] trustedCaCerts)
  {
    // Return true iff either of the following is true:
    // 1) the cert is in the trustedCaCerts.
    // 2) the cert is issued by a trusted CA.
    
    // Check whether the cert is in the trustedCaCerts
    for (int i = 0; i < trustedCaCerts.length; i++)
    {
      // If the cert has the same SubjectDN
      // as a trusted CA, check whether
      // the two certs are the same.
      if (cert.getSubjectDN().equals(trustedCaCerts[i].getSubjectDN()))
      {
        if (cert.equals(trustedCaCerts[i]))
        {
          return true;
        }
      }
    }
    
    // Check whether the cert is issued by a trusted CA.
    // Signature verification is expensive. So we check
    // whether the cert is issued
    // by one of the trusted CAs if the above loop failed.
    for (int i = 0; i < trustedCaCerts.length; i++)
    {
      // If the issuer of the cert has the same name as
      // a trusted CA, check whether that trusted CA
      // actually issued the cert.
      if (cert.getIssuerDN().equals(trustedCaCerts[i].getSubjectDN()))
      {
        try
        {
          cert.verify(trustedCaCerts[i].getPublicKey());
          return true;
        }
        catch (Exception e)
        {
          // Do nothing.
        }
      }
    }
    
    return false;
  }
  
  public static X509Certificate[] getChainRoots(Certificate[] certs)
  {
    Vector<X509Certificate> result = new Vector<X509Certificate>(3);
    // choose a Vector size that seems reasonable
    for (int i = 0; i < certs.length - 1; i++)
    {
      if (!((X509Certificate) certs[i + 1]).getSubjectDN().equals(((X509Certificate) certs[i]).getIssuerDN()))
      {
        // We've reached the end of a chain
        result.addElement((X509Certificate) certs[i]);
      }
    }
    // The final entry in the certs array is always
    // a "root" certificate
    result.addElement((X509Certificate) certs[certs.length - 1]);
    X509Certificate[] ret = new X509Certificate[result.size()];
    result.copyInto(ret);
    
    return ret;
  }
}
