package Information.Fingerprinting

import Information.Fingerprinting.Generic.{FingerprintGasSteal, FingerprintRampBlock}
import Information.Fingerprinting.ProtossStrategies._
import Information.Fingerprinting.TerranStrategies._
import Information.Fingerprinting.ZergStrategies._
import Lifecycle.With
import Strategery.Tags

import scala.collection.mutable

class Fingerprints {

  val all: mutable.ArrayBuffer[Fingerprint] = new mutable.ArrayBuffer[Fingerprint]

  def status: Seq[String] = all.filter(_()).map(Tags.tagFingerprint)

  def relevant: Seq[Fingerprint] =
    Seq(
      workerRush,
      gasSteal,
      rampBlock) ++
    (if (With.enemies.exists(_.isUnknownOrTerran)) Seq(
      bunkerRush,
      proxyRax,
      fiveRax,
      eightRax,
      bbs,
      twoRax1113,
      twoRaxAcad,
      oneRaxGas,
      oneFac,
      siegeExpand,
      fd,
      twoFac,
      twoFacVultures,
      threeFac,
      threeFacVultures,
      oneRaxFE,
      fourteenCC,
      twoPortWraith,
      oneBaseBioMech,
      bio,
    ) else Seq.empty) ++
    (if (With.enemies.exists(_.isUnknownOrProtoss)) Seq(
      gatewayFirst,
      earlyForge,
      proxyGateway,
      cannonRush,
      twoGate99,
      twoGate,
      fourGateGoon,
      threeGateGoon,
      twoGateGoon,
      dragoonRange,
      oneGateCore,
      nexusFirst,
      forgeFe,
      gatewayFe,
      robo,
      dtRush,
      coreBeforeZ,
      mannerPylon,
    ) else Seq.empty) ++
    (if (With.enemies.exists(_.isUnknownOrZerg)) Seq(
      fourPool,
      ninePool,
      ninePoolGas,
      ninePoolHatch,
      overpool,
      overpoolGas,
      overpoolHatch,
      twelvePool,
      twelvePoolGas,
      tenHatch,
      tenHatchPool,
      tenHatchPoolGas,
      twelveHatch,
      twelveHatchPool,
      twelveHatchPoolGas,
      twelveHatchPoolHatch,
      oneHatchGas,
      twoHatchGas,
      threeHatchGas,
      twoHatchMain,
      oneHatchMuta,
      twoHatchMuta,
      threeHatchMuta,
      zerglingsOnly
    ) else Seq.empty)

  private def addFingerprint(fingerprint: Fingerprint): Fingerprint = {
    all += fingerprint
    fingerprint
  }

  // Generic
  lazy val workerRush           = addFingerprint(new FingerprintWorkerRush)
  lazy val gasSteal             = addFingerprint(new FingerprintGasSteal)
  lazy val rampBlock            = addFingerprint(new FingerprintRampBlock)
  // Terran
  lazy val bunkerRush           = addFingerprint(new FingerprintBunkerRush)
  lazy val proxyRax             = addFingerprint(new FingerprintProxyRax)
  lazy val fiveRax              = addFingerprint(new Fingerprint5Rax)
  lazy val eightRax             = addFingerprint(new Fingerprint8Rax)
  lazy val bbs                  = addFingerprint(new FingerprintBBS)
  lazy val twoRax1113           = addFingerprint(new Fingerprint2Rax1113)
  lazy val twoRaxAcad           = addFingerprint(new Fingerprint2RaxAcad)
  lazy val oneRaxGas            = addFingerprint(new Fingerprint1RaxGas)
  lazy val oneFac               = addFingerprint(new Fingerprint1Fac)
  lazy val siegeExpand          = addFingerprint(new FingerprintSiegeExpand)
  lazy val fd                   = addFingerprint(new FingerprintFD)
  lazy val twoFac               = addFingerprint(new Fingerprint2Fac)
  lazy val twoFacVultures       = addFingerprint(new Fingerprint2FacVultures)
  lazy val threeFac             = addFingerprint(new Fingerprint3Fac)
  lazy val threeFacVultures     = addFingerprint(new Fingerprint3FacVultures)
  lazy val oneRaxFE             = addFingerprint(new Fingerprint1RaxFE)
  lazy val fourteenCC           = addFingerprint(new Fingerprint14CC)
  lazy val twoPortWraith        = addFingerprint(new Fingerprint2PortWraith)
  lazy val oneBaseBioMech       = addFingerprint(new Fingerprint1BaseBioMech)
  lazy val bio                  = addFingerprint(new FingerprintBio)
  lazy val oneArmoryUpgrades    = addFingerprint(new Fingerprint1ArmoryUpgrades)
  lazy val twoArmoryUpgrades    = addFingerprint(new Fingerprint2ArmoryUpgrades)
  lazy val wallIn               = addFingerprint(new FingerprintWallIn)
  // Protoss
  lazy val gatewayFirst         = addFingerprint(new FingerprintGatewayFirst)
  lazy val earlyForge           = addFingerprint(new FingerprintEarlyForge)
  lazy val proxyGateway         = addFingerprint(new FingerprintProxyGateway)
  lazy val cannonRush           = addFingerprint(new FingerprintCannonRush)
  lazy val twoGate              = addFingerprint(new Fingerprint2Gate)
  lazy val twoGate99            = addFingerprint(new Fingerprint2Gate99)
  lazy val oneGateCore          = addFingerprint(new Fingerprint1GateCore)
  lazy val robo                 = addFingerprint(new FingerprintRobo)
  lazy val twoGateGoon          = addFingerprint(new Fingerprint2GateGoon)
  lazy val threeGateGoon        = addFingerprint(new Fingerprint3GateGoon)
  lazy val fourGateGoon         = addFingerprint(new Fingerprint4GateGoon)
  lazy val nexusFirst           = addFingerprint(new FingerprintNexusFirst)
  lazy val forgeFe              = addFingerprint(new FingerprintForgeFE)
  lazy val gatewayFe            = addFingerprint(new FingerprintGatewayFE)
  lazy val dtRush               = addFingerprint(new FingerprintDTRush)
  lazy val dragoonRange         = addFingerprint(new FingerprintDragoonRange)
  lazy val coreBeforeZ          = addFingerprint(new FingerprintCoreBeforeZealot)
  lazy val mannerPylon          = addFingerprint(new FingerprintMannerPylon)
  // Zerg
  lazy val fourPool             = addFingerprint(new Fingerprint4Pool)
  lazy val ninePool             = addFingerprint(new Fingerprint9Pool)
  lazy val ninePoolGas          = addFingerprint(new Fingerprint9PoolGas)
  lazy val ninePoolHatch        = addFingerprint(new Fingerprint9PoolHatch)
  lazy val overpool             = addFingerprint(new FingerprintOverpool)
  lazy val overpoolGas          = addFingerprint(new FingerprintOverpoolGas)
  lazy val overpoolHatch        = addFingerprint(new FingerprintOverpoolHatch)
  lazy val twelvePool           = addFingerprint(new Fingerprint12Pool)
  lazy val twelvePoolGas        = addFingerprint(new Fingerprint12Pool11Gas)
  lazy val hatchFirst           = addFingerprint(new FingerprintHatchFirst)
  lazy val tenHatch             = addFingerprint(new Fingerprint10Hatch)
  lazy val tenHatchPool         = addFingerprint(new Fingerprint10Hatch9Pool)
  lazy val tenHatchPoolGas      = addFingerprint(new Fingerprint10Hatch9Pool8Gas)
  lazy val twelveHatch          = addFingerprint(new Fingerprint12Hatch)
  lazy val twelveHatchPool      = addFingerprint(new Fingerprint12Hatch11Pool)
  lazy val twelveHatchPoolHatch = addFingerprint(new Fingerprint12Hatch11Pool13Hatch)
  lazy val twelveHatchPoolGas   = addFingerprint(new Fingerprint12Hatch11Pool10Gas)
  lazy val twelveHatchHatch     = addFingerprint(new Fingerprint12HatchHatch)
  lazy val oneHatchGas          = addFingerprint(new Fingerprint1HatchGas)
  lazy val twoHatchGas          = addFingerprint(new Fingerprint2HatchGas)
  lazy val threeHatchGas        = addFingerprint(new Fingerprint3HatchGas)
  lazy val twoHatchMain         = addFingerprint(new Fingerprint2HatchMain)
  lazy val oneHatchMuta         = addFingerprint(new Fingerprint1HatchMuta)
  lazy val twoHatchMuta         = addFingerprint(new Fingerprint2HatchMuta)
  lazy val threeHatchMuta       = addFingerprint(new Fingerprint3HatchMuta)
  lazy val threeHatchHydra      = addFingerprint(new Fingerprint3HatchHydra)
  lazy val speedBeforeLair      = addFingerprint(new FingerprintSpeedBeforeLair)
  lazy val zerglingsOnly        = addFingerprint(new FingerprintZerglingOnly)
}
