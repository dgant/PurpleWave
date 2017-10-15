package Strategery.Strategies

import Planning.Plan
import Strategery.Maps.StarCraftMap
import bwapi.Race

abstract class Strategy {
  
  override def toString: String = getClass.getSimpleName.replace("$", "")
  
  def gameplan: Option[Plan] = { None }
  
  def choices: Iterable[Iterable[Strategy]] = Iterable.empty
  
  def islandMaps          : Boolean                   = false
  def groundMaps          : Boolean                   = true
  def multipleEntrances   : Boolean                   = true
  def ourRaces            : Iterable[Race]            = Vector(Race.Terran, Race.Protoss, Race.Zerg)
  def enemyRaces          : Iterable[Race]            = Vector(Race.Terran, Race.Protoss, Race.Zerg, Race.Unknown)
  def startLocationsMin   : Int                       = 2
  def startLocationsMax   : Int                       = 24
  def ffa                 : Boolean                   = false
  def restrictedOpponents : Option[Iterable[String]]  = None
  def prohibitedMaps      : Iterable[StarCraftMap]    = Vector.empty
}
