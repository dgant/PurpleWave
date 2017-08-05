package Micro.Squads.Goals

import Micro.Squads.Squad

trait SquadGoal {
  
  def update(squad: Squad)
  
  override def toString: String = getClass.getSimpleName.replaceAllLiterally("$", "")
  
  def fullyEquipped         : Boolean = true
  def acceptsHelp           : Boolean = true
  def requiresAntiAir       : Boolean = false
  def requiresAntiGround    : Boolean = false
  def requiresDetectors     : Boolean = false
  def requiresHealers       : Boolean = false
  def requiresRepairers     : Boolean = false
  def requiresSplashAir     : Boolean = false
  def requiresSplashGround  : Boolean = false
  def requiresBuilders      : Boolean = false
  def requiresSpotters      : Boolean = false
  def requiresTransport     : Boolean = false
  def requiresSiege         : Boolean = false
}
