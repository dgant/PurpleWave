package Information.Intelligenze.Fingerprinting

import Information.Intelligenze.Fingerprinting.ProtossStrategies._
import Information.Intelligenze.Fingerprinting.ZergStrategies._
import Lifecycle.With

import scala.collection.mutable

class Fingerprints {
  
  def update() {
    if (With.enemies.exists(_.isProtoss)) {
      gatewayFirst
      proxyGateway
      cannonRush
      twoGate
      oneGateCore
      nexusFirst
      forgeFe
      gatewayFe
      dtRush
    }
    if (With.enemies.exists(_.isZerg)) {
      fourPool
      ninePool
      overpool
      tenHatch
      twelvePool
      twelveHatch
    }
    all.foreach(_.update())
  }
  
  val all: mutable.ArrayBuffer[Fingerprint] = new mutable.ArrayBuffer[Fingerprint]
  
  private def addFingerprint(fingerprint: Fingerprint): Fingerprint = {
    all += fingerprint
    fingerprint
  }
  
  // Protoss
  lazy val gatewayFirst = addFingerprint(new FingerprintGatewayFirst)
  lazy val proxyGateway = addFingerprint(new FingerprintProxyGateway)
  lazy val cannonRush   = addFingerprint(new FingerprintCannonRush)
  lazy val twoGate      = addFingerprint(new Fingerprint2Gate)
  lazy val oneGateCore  = addFingerprint(new Fingerprint1GateCore)
  lazy val nexusFirst   = addFingerprint(new FingerprintNexusFirst)
  lazy val forgeFe      = addFingerprint(new FingerprintForgeFE)
  lazy val gatewayFe    = addFingerprint(new FingerprintGatewayFE)
  lazy val dtRush       = addFingerprint(new FingerprintDTRush)
  
  // Zerg
  lazy val fourPool     = addFingerprint(new Fingerprint4Pool)
  lazy val ninePool     = addFingerprint(new Fingerprint9Pool)
  lazy val overpool     = addFingerprint(new FingerprintOverpool)
  lazy val tenHatch     = addFingerprint(new Fingerprint10Hatch9Pool)
  lazy val twelvePool   = addFingerprint(new Fingerprint12Pool)
  lazy val twelveHatch  = addFingerprint(new Fingerprint12Hatch)
}
