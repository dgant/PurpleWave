package Strategery

import Debugging.ToString
import Information.Geography.NeoGeo.MapIdentifier
import Lifecycle.With
abstract class StarCraftMap {
  val name: String = ToString(this)
  val nameStub: String = MapIdentifier(name)
  def apply(): Boolean = nameStub.contains(With.mapCleanName) || With.mapCleanName.contains(nameStub)
}
object Alchemist extends StarCraftMap
object Arcadia extends StarCraftMap
object Aztec extends StarCraftMap
object Benzene extends StarCraftMap
object BlueStorm extends StarCraftMap
object ChupungRyeong extends StarCraftMap
object CircuitBreaker extends StarCraftMap
object Destination extends StarCraftMap
object Eddy extends StarCraftMap
object EmpireOfTheSun extends StarCraftMap
object Gladiator extends StarCraftMap
object GoldRush extends StarCraftMap
object GreatBarrierReef extends StarCraftMap
object Heartbreak extends StarCraftMap
object Hitchhiker extends StarCraftMap
object Hunters extends StarCraftMap
object LaMancha extends StarCraftMap
object Longinus extends StarCraftMap
object Luna extends StarCraftMap
object MatchPoint extends StarCraftMap
object Medusa extends StarCraftMap
object Pathfinder extends StarCraftMap
object Plasma extends StarCraftMap
object PolarisRhapsody extends StarCraftMap
object Python extends StarCraftMap
object Roadkill extends StarCraftMap
object Roadrunner extends StarCraftMap
object Sparkle extends StarCraftMap
object TauCross extends StarCraftMap
object ThirdWorld extends StarCraftMap
object Transistor extends StarCraftMap

object StarCraftMaps {
  val all: Vector[StarCraftMap] = Vector(
    Alchemist,
    Arcadia,
    Benzene,
    BlueStorm,
    ChupungRyeong,
    Eddy,
    EmpireOfTheSun,
    Gladiator,
    GoldRush,
    GreatBarrierReef,
    Heartbreak,
    Hitchhiker,
    Hunters,
    LaMancha,
    Pathfinder,
    Plasma,
    Python,
    Roadrunner,
    Sparkle,
    TauCross,
    ThirdWorld,
    Transistor
  )
}

object MapGroups {
  val badForProxying: Vector[StarCraftMap] = Vector(Alchemist, BlueStorm, ChupungRyeong, Eddy, EmpireOfTheSun, GreatBarrierReef, LaMancha, Roadrunner, TauCross, Arcadia, Luna)
  val badForMassGoon: Vector[StarCraftMap] = Vector(Destination) // We've allowed some maps like Aztec, Match Point, Circuit Breaker and Roadrunner because we now we *should* know how to flank on those
  val narrowRamp    : Vector[StarCraftMap] = Vector(Plasma, ThirdWorld)
  val strongNatural : Vector[StarCraftMap] = Vector(Aztec, MatchPoint, Roadrunner, Destination, Roadkill)
}

