package Macro.Requests

import ProxyBwapi.Races.Neutral

object RequestAutosupply extends RequestBuildable(Neutral.Autosupply) {
  override val toString = "Autosupply"
}