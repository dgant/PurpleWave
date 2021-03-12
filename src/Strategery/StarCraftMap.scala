package Strategery

import Debugging.ToString
import Lifecycle.With
abstract class StarCraftMap {
  val name: String = ToString(this)
  val nameStub: String = StarCraftMapMatcher.clean(name)
  def matches: Boolean = nameStub.contains(With.mapId) || With.mapId.contains(nameStub)
  var mineralWalkingOkay: Boolean = true
}
object Alchemist extends StarCraftMap
object Arcadia extends StarCraftMap
object Benzene extends StarCraftMap
object BlueStorm extends StarCraftMap
object ChupungRyeong extends StarCraftMap
object Destination extends StarCraftMap
object Eddy extends StarCraftMap
object EmpireOfTheSun extends StarCraftMap
object Gladiator extends StarCraftMap
object GoldRush extends StarCraftMap
object GreatBarrierReef extends StarCraftMap { mineralWalkingOkay = false }
object Heartbreak extends StarCraftMap
object Hitchhiker extends StarCraftMap
object Hunters extends StarCraftMap
object LaMancha extends StarCraftMap
object Luna extends StarCraftMap
object Medusa extends StarCraftMap
object Pathfinder extends StarCraftMap
object Plasma extends StarCraftMap
object Python extends StarCraftMap
object Roadrunner extends StarCraftMap
object Sparkle extends StarCraftMap
object TauCross extends StarCraftMap
object ThirdWorld extends StarCraftMap
object Transistor extends StarCraftMap

object StarCraftMaps {

  val all: Vector[StarCraftMap] = Vector(
    Alchemist,
    Benzene,
    BlueStorm,
    ChupungRyeong,
    Eddy,
    EmpireOfTheSun,
    GoldRush,
    Gladiator,
    GreatBarrierReef,
    Heartbreak,
    Hitchhiker,
    Hunters,
    LaMancha,
    Pathfinder,
    Plasma,
    Roadrunner,
    Sparkle,
    TauCross,
    ThirdWorld,
    Transistor
  )
}

object MapGroups {
  val badForProxying = Vector(Alchemist, BlueStorm, ChupungRyeong, Eddy, EmpireOfTheSun, GreatBarrierReef, LaMancha, Roadrunner, TauCross, Arcadia, Luna)
  val badForWalling = Vector(Alchemist, Pathfinder)
  val tooShortForFFE = Vector(Python)
  val narrowRamp = Vector(Plasma, ThirdWorld)
  val needCustomPathing = Vector(Benzene, BlueStorm, GoldRush)
}

