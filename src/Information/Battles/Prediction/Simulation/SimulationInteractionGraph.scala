package Information.Battles.Prediction.Simulation

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

final class SimulationInteractionGraph(simulation: Simulation) {
  private final val ProximityBufferPixels = 64.0
  private final val SearchPaddingPixels   = 128.0
  private var proximityFrames = 0
  private var componentCount = 0

  private var units   : Array[UnitInfo] = Array.empty
  private var parent  : Array[Int]      = Array.empty
  private var size    : Array[Int]      = Array.empty
  private val indices = new mutable.HashMap[UnitInfo, Int]

  def reset(newUnits: Seq[UnitInfo]): Unit = {
    units = newUnits.toArray
    componentCount = units.length
    proximityFrames = Math.max(0, With.configuration.simulationProximityFrames)
    parent = new Array[Int](units.length)
    size = Array.fill(units.length)(1)
    indices.clear()
    var i = 0
    while (i < units.length) {
      parent(i) = i
      indices(units(i)) = i
      i += 1
    }
  }

  // Damage, including late hits and friendly splash, keeps scored outcomes causally connected.
  @inline def observeDamage(attacker: UnitInfo, victim: UnitInfo): Unit = union(attacker, victim)

  def observeProximity(): Unit = {
    if (componentCount <= 1 || simulation.battle.simulationFrames > proximityFrames) return
    var i = 0
    while (i < units.length && componentCount > 1) {
      val simulacrum = units(i).simulacrum
      if (simulacrum.alive) {
        observeProximity(simulacrum, i)
      }
      i += 1
    }
  }

  def components: Vector[Seq[UnitInfo]] = {
    val output = new mutable.LinkedHashMap[Int, mutable.ArrayBuffer[UnitInfo]]
    var i = 0
    while (i < units.length) {
      val root = find(i)
      output.getOrElseUpdate(root, new mutable.ArrayBuffer[UnitInfo]) += units(i)
      i += 1
    }
    output.values.map(_.toVector).toVector
  }

  private def observeProximity(unit: Simulacrum, unitIndex: Int): Unit = {
    val rangePixels = unit.effectiveRangePixels + ProximityBufferPixels + SearchPaddingPixels
    val rangeTiles = Math.ceil(rangePixels / 32.0).toInt
    val tile = unit.pixel.tile.clip

    simulation.grid.populatedTiles.foreach(gridTile => {
      if (Math.abs(gridTile.tile.x - tile.x) <= rangeTiles && Math.abs(gridTile.tile.y - tile.y) <= rangeTiles) {
        val tileUnits = gridTile.units
        var i = 0
        while (i < tileUnits.length) {
          val other = tileUnits(i)
          if (other != unit && other.alive && unit.isEnemyOf(other.realUnit)) {
            val otherIndex = indices.getOrElse(other.realUnit, -1)
            // Existing connections cannot change the partition, regardless of distance.
            if (otherIndex >= 0 && find(unitIndex) != find(otherIndex) && proximal(unit, other)) {
              union(unitIndex, otherIndex)
            }
          }
          i += 1
        }
      }
    })
  }

  @inline private def proximal(unit: Simulacrum, other: Simulacrum): Boolean = {
    unit.pixelDistanceEdgeSimulation(other) <= Math.max(unit.effectiveRangePixels, other.effectiveRangePixels) + ProximityBufferPixels
  }

  @inline private def union(unitA: UnitInfo, unitB: UnitInfo): Unit = {
    val a = indices.get(unitA)
    val b = indices.get(unitB)
    if (a.isEmpty || b.isEmpty) return
    union(a.get, b.get)
  }

  @inline private def union(a: Int, b: Int): Unit = {
    var rootA = find(a)
    var rootB = find(b)
    if (rootA == rootB) return

    if (size(rootA) < size(rootB)) {
      val swap = rootA
      rootA = rootB
      rootB = swap
    }
    parent(rootB) = rootA
    size(rootA) += size(rootB)
    componentCount -= 1
  }

  @inline private def find(i: Int): Int = {
    var root = i
    while (parent(root) != root) {
      root = parent(root)
    }
    var next = i
    while (parent(next) != root) {
      val current = next
      next = parent(next)
      parent(current) = root
    }
    root
  }
}
