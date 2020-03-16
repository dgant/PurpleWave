package Information.Fingerprinting

import Information.Fingerprinting.Generic.GameTime
import Information.Fingerprinting.ProtossStrategies._
import Information.Fingerprinting.TerranStrategies._
import Information.Fingerprinting.ZergStrategies._
import Lifecycle.With

import scala.collection.mutable

class Fingerprints {
  
  def update() {
    if (With.enemies.exists(_.isUnknownOrTerran)) {
      bunkerRush
      fiveRax
      bbs
      twoRax1113
      twoFac
      twoFacVultures
      threeFac
      threeFacVultures
      siegeExpand
      oneRaxFE
      fourteenCC
      bio
    }
    if (With.enemies.exists(_.isUnknownOrProtoss)) {
      gatewayFirst
      earlyForge
      proxyGateway
      cannonRush
      twoGate
      oneGateCore
      fourGateGoon
      nexusFirst
      forgeFe
      gatewayFe
      dtRush
      dragoonRange
      mannerPylon
    }
    if (With.enemies.exists(_.isUnknownOrZerg)) {
      fourPool
      ninePool
      overpool
      tenHatch
      twelvePool
      twelveHatch
      oneHatchGas
    }
    workerRush
    gasSteal

    if (With.frame < GameTime(10, 0)()) {
      all.foreach(_.update())
    }
  }

  val all: mutable.ArrayBuffer[Fingerprint] = new mutable.ArrayBuffer[Fingerprint]

  private def addFingerprint(fingerprint: Fingerprint): Fingerprint = {
    all += fingerprint
    fingerprint
  }

  def status: Seq[String] = all.filter(_.matches).map(_.toString.replaceAll("Fingerprint", ""))

  // Generic
  lazy val workerRush       = addFingerprint(new FingerprintWorkerRush)
  lazy val gasSteal         = addFingerprint(new FingerprintGasSteal)

  // Terran
  lazy val bunkerRush         = addFingerprint(new FingerprintBunkerRush)
  lazy val fiveRax            = addFingerprint(new Fingerprint5Rax)
  lazy val bbs                = addFingerprint(new FingerprintBBS)
  lazy val twoRax1113         = addFingerprint(new Fingerprint2Rax1113)
  lazy val twoRaxAcad         = addFingerprint(new Fingerprint2RaxAcad)
  lazy val oneRaxGas          = addFingerprint(new Fingerprint1RaxGas)
  lazy val oneFac             = addFingerprint(new Fingerprint1Fac)
  lazy val twoFac             = addFingerprint(new Fingerprint2Fac)
  lazy val twoFacVultures     = addFingerprint(new Fingerprint2FacVultures)
  lazy val threeFac           = addFingerprint(new Fingerprint3Fac)
  lazy val threeFacVultures   = addFingerprint(new Fingerprint3FacVultures)
  lazy val siegeExpand        = addFingerprint(new FingerprintSiegeExpand)
  lazy val oneRaxFE           = addFingerprint(new Fingerprint1RaxFE)
  lazy val fourteenCC         = addFingerprint(new Fingerprint14CC)
  lazy val bio                = addFingerprint(new FingerprintBio)
  lazy val oneArmoryUpgrades  = addFingerprint(new Fingerprint1ArmoryUpgrades)
  lazy val twoArmoryUpgrades  = addFingerprint(new Fingerprint2ArmoryUpgrades)
  lazy val wallIn             = addFingerprint(new FingerprintWallIn)

  // Protoss
  lazy val gatewayFirst = addFingerprint(new FingerprintGatewayFirst)
  lazy val earlyForge   = addFingerprint(new FingerprintEarlyForge)
  lazy val proxyGateway = addFingerprint(new FingerprintProxyGateway)
  lazy val cannonRush   = addFingerprint(new FingerprintCannonRush)
  lazy val twoGate      = addFingerprint(new Fingerprint2Gate)
  lazy val oneGateCore  = addFingerprint(new Fingerprint1GateCore)
  lazy val robo         = addFingerprint(new FingerprintRobo)
  lazy val fourGateGoon = addFingerprint(new Fingerprint4GateGoon)
  lazy val nexusFirst   = addFingerprint(new FingerprintNexusFirst)
  lazy val forgeFe      = addFingerprint(new FingerprintForgeFE)
  lazy val gatewayFe    = addFingerprint(new FingerprintGatewayFE)
  lazy val dtRush       = addFingerprint(new FingerprintDTRush)
  lazy val dragoonRange = addFingerprint(new FingerprintDragoonRange)
  lazy val mannerPylon  = addFingerprint(new FingerprintMannerPylon)
  
  // Zerg
  lazy val fourPool     = addFingerprint(new Fingerprint4Pool)
  lazy val ninePool     = addFingerprint(new Fingerprint9Pool)
  lazy val ninePoolGas  = addFingerprint(new Fingerprint9PoolGas)
  lazy val overpool     = addFingerprint(new FingerprintOverpool)
  lazy val tenHatch     = addFingerprint(new Fingerprint10Hatch9Pool)
  lazy val twelvePool   = addFingerprint(new Fingerprint12Pool)
  lazy val twelveHatch  = addFingerprint(new Fingerprint12Hatch)
  lazy val oneHatchGas  = addFingerprint(new Fingerprint1HatchGas)
}
