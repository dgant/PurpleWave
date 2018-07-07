package Strategery

import Lifecycle.With
abstract class StarCraftMap {
  
  def name: String = this.getClass.getSimpleName
  
  def matches: Boolean = {
    val nameStub  = clean(name)
    val fileStub  = clean(With.mapFileName)
    val output    = nameStub.contains(fileStub) || fileStub.contains(nameStub)
    output
  }
  
  private def clean(mapString: String): String = {
    mapString.toLowerCase.replaceAll("[^a-z]", "")
  }
  
  val mineralWalkingOkay: Boolean = true
  val trustGroundDistance: Boolean = true
}
object Alchemist extends StarCraftMap
object Benzene extends StarCraftMap
object BlueStorm extends StarCraftMap
object ChupungRyeong extends StarCraftMap
object EmpireOfTheSun extends StarCraftMap
object Gladiator extends StarCraftMap
object GreatBarrierReef extends StarCraftMap { override val mineralWalkingOkay = false }
object HeartbreakRidge extends StarCraftMap
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
    HeartbreakRidge,
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
  val badForProxying = Vector(Alchemist, BlueStorm, ChupungRyeong, EmpireOfTheSun, GreatBarrierReef, LaMancha, Roadrunner, TauCross)
  val badForWalling = Vector(Alchemist)
  val badForFastThirdBases = Vector(Benzene, HeartbreakRidge)
}

