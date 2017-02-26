package Types.UnitInfo

import Startup.With
import bwapi._

abstract class UnitInfo {
  
  def id:Int;
  def lastSeen:Int;
  def possiblyStillThere:Boolean;
  def player:Player;
  def position:Position;
  def walkPosition:WalkPosition;
  def tilePosition:TilePosition;
  def hitPoints:Int;
  def shieldPoints:Int;
  def unitType:UnitType;
  def complete:Boolean;
  
  def unit:Option[bwapi.Unit] = With.unit(id)
}
