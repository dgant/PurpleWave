package Micro.Squads.Goals

import Micro.Squads.Squad

trait SettableSquad {
  def squad: Squad = _squad
  def setSquad(squad: Squad): Unit = { _squad = squad }
  private var _squad: Squad = _
}