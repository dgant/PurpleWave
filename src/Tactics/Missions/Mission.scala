package Tactics.Missions

import Micro.Squads.Squad

trait Mission extends Squad {
  def shouldForm: Boolean
  def shouldTerminate: Boolean
}
