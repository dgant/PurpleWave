package Tactics.Missions

import Tactics.Squads.Squad

trait Mission extends Squad {
  def shouldForm: Boolean
  def shouldTerminate: Boolean
}
