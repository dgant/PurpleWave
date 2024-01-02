package Placement

import Lifecycle.With.game
import bwta.BWTA
import jbweb.{Blocks, JBWEB, Stations}

object JBWEBWrapper {

  def enabled: Boolean = false

  def onStart(): Unit = {
    if ( ! enabled) return
    JBWEB.onStart(game, BWTA.getBWEM)
    jbweb.Walls.createFFE(2)
    Stations.findStations()
    Blocks.findBlocks()
  }

  def onUnitDestroy(unit: bwapi.Unit): Unit = {
    if ( ! enabled) return
  }

  def onUnitDiscover(unit: bwapi.Unit): Unit = {
    if ( ! enabled) return
    JBWEB.onUnitDiscover(unit)
  }

  def onUnitMorph(unit: bwapi.Unit): Unit = {
    if ( ! enabled) return
    JBWEB.onUnitMorph(unit)
  }

  def draw(): Unit = {
    if ( ! enabled) return
    JBWEB.draw()
  }
}
