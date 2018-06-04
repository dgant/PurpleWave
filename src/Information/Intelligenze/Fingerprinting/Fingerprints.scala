package Information.Intelligenze.Fingerprinting

import Information.Intelligenze.Fingerprinting.ProtossStrategies._
import Information.Intelligenze.Fingerprinting.ZergStrategies._

import scala.collection.mutable

class Fingerprints {
  
  val all: mutable.ArrayBuffer[Fingerprint] = new mutable.ArrayBuffer[Fingerprint]
  
  private def addFingerprint(fingerprint: Fingerprint): Fingerprint = {
    all += fingerprint
    fingerprint
  }
  
  lazy val proxyGateway     = addFingerprint(new FingerprintProxyGateway)
  lazy val cannonRush       = addFingerprint(new FingerprintCannonRush)
  lazy val twoGate          = addFingerprint(new Fingerprint2Gate)
  lazy val oneGateCore      = addFingerprint(new Fingerprint1GateCore)
  lazy val nexusFirst       = addFingerprint(new FingerprintNexusFirst)
  lazy val forgeFe          = addFingerprint(new FingerprintForgeFE)
  lazy val gatewayFe        = addFingerprint(new FingerprintGatewayFE)
  
  lazy val fourPool         = addFingerprint(new Fingerprint4Pool)
  lazy val ninePool         = addFingerprint(new Fingerprint9Pool)
  lazy val overpool         = addFingerprint(new FingerprintOverpool)
  lazy val tenHatchNinePool = addFingerprint(new Fingerprint10Hatch9Pool)
  lazy val twelvePool       = addFingerprint(new Fingerprint12Pool)
  lazy val twelveHatch      = addFingerprint(new Fingerprint12Hatch)
}
