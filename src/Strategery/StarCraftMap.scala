package Strategery

import Lifecycle.With
abstract class StarCraftMap {
  
  val name: String = this.getClass.getSimpleName
  val nameStub  = StarCraftMapMatcher.clean(name)

  def matches: Boolean = nameStub.contains(With.mapId) || With.mapId.contains(nameStub)
  
  val mineralWalkingOkay: Boolean = true
  val trustGroundDistance: Boolean = true
}
object Alchemist extends StarCraftMap
object Benzene extends StarCraftMap
object BlueStorm extends StarCraftMap
object ChupungRyeong extends StarCraftMap
object Destination extends StarCraftMap
object EmpireOfTheSun extends StarCraftMap
object Gladiator extends StarCraftMap
object GreatBarrierReef extends StarCraftMap { override val mineralWalkingOkay = false }
object Heartbreak extends StarCraftMap
object Hitchhiker extends StarCraftMap
object Hunters extends StarCraftMap
object LaMancha extends StarCraftMap
object Plasma extends StarCraftMap { override val trustGroundDistance: Boolean = false }
object Roadrunner extends StarCraftMap
object Sparkle extends StarCraftMap
object TauCross extends StarCraftMap
object ThirdWorld extends StarCraftMap { override val trustGroundDistance: Boolean = false }
object Transistor extends StarCraftMap

object StarCraftMaps {

  val all: Vector[StarCraftMap] = Vector(
    Alchemist,
    Benzene,
    BlueStorm,
    ChupungRyeong,
    EmpireOfTheSun,
    Gladiator,
    GreatBarrierReef,
    Heartbreak,
    Hitchhiker,
    Hunters,
    LaMancha,
    Plasma,
    Roadrunner,
    Sparkle,
    TauCross,
    ThirdWorld,
    Transistor
  )
}

object MapGroups {
  val badForBigUnits = Vector(BlueStorm)
  val badForProxying = Vector(Alchemist, BlueStorm, ChupungRyeong, EmpireOfTheSun, GreatBarrierReef, LaMancha, Roadrunner, TauCross)
  val badForWalling = Vector(Alchemist)
  val badForFastThirdBases = Vector(Benzene, Heartbreak)
}

