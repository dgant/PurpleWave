package Strategery.Maps

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
}

object EmpireOfTheSun extends StarCraftMap
object Roadrunner extends StarCraftMap
object TauCross extends StarCraftMap
object LaMancha extends StarCraftMap
object Benzene extends StarCraftMap
object Hunters extends StarCraftMap