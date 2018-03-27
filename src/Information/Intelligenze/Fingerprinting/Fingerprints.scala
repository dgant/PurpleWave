package Information.Intelligenze.Fingerprinting

import Information.Intelligenze.Fingerprinting.ProtossStrategies.{Fingerprint1GateCore, Fingerprint2Gate, FingerprintProxyGateway}
import Information.Intelligenze.Fingerprinting.ZergStrategies._

import scala.collection.mutable

class Fingerprints {
  
  val all: mutable.ArrayBuffer[Fingerprint] = new mutable.ArrayBuffer[Fingerprint]
  
  private def addFingerprint(fingerprint: Fingerprint): Fingerprint = {
    all += fingerprint
    fingerprint
  }
  
  lazy val fingerprintProxyGateway = addFingerprint(new FingerprintProxyGateway)
  lazy val fingerprint2Gate        = addFingerprint(new Fingerprint2Gate)
  lazy val fingerprint1GateCore    = addFingerprint(new Fingerprint1GateCore)
  lazy val fingerprint4Pool        = addFingerprint(new Fingerprint4Pool)
  lazy val fingerprint9Pool        = addFingerprint(new Fingerprint9Pool)
  lazy val fingerprintOverpool     = addFingerprint(new FingerprintOverpool)
  lazy val fingerprint10Hatch9Pool = addFingerprint(new Fingerprint10Hatch9Pool)
  lazy val fingerprint12Pool       = addFingerprint(new Fingerprint12Pool)
  lazy val fingerprint12Hatch      = addFingerprint(new Fingerprint12Hatch)
}
