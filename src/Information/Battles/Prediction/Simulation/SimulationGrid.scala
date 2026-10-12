package Information.Battles.Prediction.Simulation

import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import ProxyBwapi.Races.Protoss
import Mathematics.Maff
import ProxyBwapi.UnitInfo.{DamageVolley, UnitInfo}
import bwapi.{ExplosionType, WeaponType}

import scala.collection.mutable
import java.util.concurrent.ThreadLocalRandom

final class SimulationGrid(val collisionEnabled: Boolean = true) {
  val occupancyMax: Int = Protoss.Dragoon.area
  val tiles: Array[SimulationGridTile] = (0 until With.mapTileArea).map(new SimulationGridTile(_)).toArray

  val populatedTiles: mutable.Set[SimulationGridTile] = new mutable.HashSet[SimulationGridTile]
  private var frame = 0
  private var maxUnitExtent = 0
  private val fallbackTiles = new Array[SimulationGridTile](4)
  private val fallbackCosts = new Array[Double](4)
  private val fallbackBacktracks = new Array[Boolean](4)

  @inline def advanceFrame(): Unit = { frame += 1 }

  def reset(): Unit = {
    populatedTiles.foreach(_.reset())
    populatedTiles.clear()
    maxUnitExtent = 0
    frame += 2 // Expire requests even on cells that became empty before reset.
  }

  def populate(unit: Simulacrum): Unit = {
    maxUnitExtent = Math.max(maxUnitExtent, unit.unitClass.dimensionMax)
    val gridTile = tileAt(unit.pixel)
    gridTile += unit
    unit.gridTile = Some(gridTile)
    populatedTiles += gridTile
  }

  // Cells store centers, so expand the search for large footprints before testing exact splash distance.
  def applySplash(primary: Simulacrum, volley: DamageVolley, sourceIsFriendly: Boolean,
    source: UnitInfo, origin: Pixel, attacker: Simulacrum = null): Unit = {
    if ( ! collisionEnabled) return
    val weapon = volley.weapon
    if (weapon == WeaponType.Glave_Wurm || weapon == WeaponType.Glave_Wurm_Kukulza) {
      applyGlaveBounces(primary, volley, sourceIsFriendly, source, attacker)
      return
    }
    val explosion = weapon.explosionType
    if (weapon.outerSplashRadius <= 0
      || (explosion != ExplosionType.Radial_Splash && explosion != ExplosionType.Enemy_Splash && explosion != ExplosionType.Air_Splash)) return
    val airSplash = explosion == ExplosionType.Air_Splash
    val friendlyFire = explosion == ExplosionType.Radial_Splash
    val lineSplash = weapon == WeaponType.Subterranean_Spines && origin != null
    val impact = primary.pixel
    val startX = if (lineSplash) origin.x else impact.x
    val startY = if (lineSplash) origin.y else impact.y
    val length = if (lineSplash) Math.hypot(impact.x - startX, impact.y - startY) else 0.0
    val endX = if (length > 0) startX + (impact.x - startX) * weapon.maxRange / length else impact.x.toDouble
    val endY = if (length > 0) startY + (impact.y - startY) * weapon.maxRange / length else impact.y.toDouble
    val radius = weapon.outerSplashRadius + maxUnitExtent
    val xMin = Maff.clamp(Maff.div32(Math.floor(Math.min(startX, endX)).toInt - radius), 0, With.mapTileWidth - 1)
    val xMax = Maff.clamp(Maff.div32(Math.ceil(Math.max(startX, endX)).toInt + radius), 0, With.mapTileWidth - 1)
    val yMin = Maff.clamp(Maff.div32(Math.floor(Math.min(startY, endY)).toInt - radius), 0, With.mapTileHeight - 1)
    val yMax = Maff.clamp(Maff.div32(Math.ceil(Math.max(startY, endY)).toInt + radius), 0, With.mapTileHeight - 1)
    var y = yMin
    while (y <= yMax) {
      var x = xMin
      while (x <= xMax) {
        val units = tiles(x + y * With.mapTileWidth).units
        var i = 0
        while (i < units.size) {
          val victim = units(i)
          if ((victim ne primary) && (victim.realUnit ne source) && victim.alive && victim.hitPoints > 0
            && ! victim.invincible && ! victim.stasised
            && (friendlyFire || victim.isFriendlyCached != sourceIsFriendly)
            && (if (victim.flying) weapon.targetsAir else weapon.targetsGround)
            && ( ! weapon.targetsMechanical || victim.unitClass.isMechanical)
            && ( ! weapon.targetsOrganic || victim.unitClass.isOrganic)
            && ( ! weapon.targetsNonBuilding || ! victim.unitClass.isBuilding)
            && ( ! weapon.targetsNonRobotic || ! victim.unitClass.isRobotic)
            && ( ! weapon.targetsOrgOrMech || victim.unitClass.isOrganic || victim.unitClass.isMechanical)
            && ( ! airSplash || victim.unitClass != Protoss.Interceptor)) {
            val distance = if (lineSplash) SimulationSplash.lineDistance(victim, startX, startY, endX, endY)
              else SimulationSplash.pointDistance(victim, impact.x, impact.y)
            val divisor = if (distance > weapon.outerSplashRadius) 0
              else if (lineSplash) { if (distance <= weapon.innerSplashRadius) 1 else 0 }
              else if ( ! airSplash && distance <= weapon.innerSplashRadius) 1
              else if (victim.burrowed) 0
              else if (distance <= weapon.medianSplashRadius) 2
              else 4
            if (divisor > 0) {
              val splash = volley.copy(damagePerHit = volley.damagePerHit / divisor,
                armorHealth = victim.armorHealth, armorShield = victim.armorShield,
                healthMultiplier = SimulationSplash.healthMultiplier(weapon, victim.unitClass.size))
              val damage = splash(victim.shieldPoints)
              if (attacker == null) {
                if (victim.receiveDamage(damage) > 0) {
                  victim.simulation.interactions.observeDamage(source, victim.realUnit)
                  // A committed projectile can outlive its source's membership in the battle.
                  victim.simulation.interactions.observeDamage(primary.realUnit, victim.realUnit)
                }
              } else attacker.applyAttackDamage(victim, damage)
            }
          }
          i += 1
        }
        x += 1
      }
      y += 1
    }
  }

  private def applyGlaveBounces(primary: Simulacrum, volley: DamageVolley, sourceIsFriendly: Boolean,
    source: UnitInfo, attacker: Simulacrum): Unit = {
    var previous = primary
    var divisor = 3
    while (divisor <= 9) {
      val chosen = sampleGlaveTarget(previous, primary, sourceIsFriendly, source)
      if (chosen == null) return
      val damage = volley(chosen.shieldPoints, volley.damagePerHit / divisor, chosen.armorHealth, chosen.armorShield, 1.0)
      if (attacker == null) {
        if (chosen.receiveDamage(damage) > 0) {
          chosen.simulation.interactions.observeDamage(source, chosen.realUnit)
          chosen.simulation.interactions.observeDamage(primary.realUnit, chosen.realUnit)
        }
      } else attacker.applyAttackDamage(chosen, damage)
      previous = chosen
      divisor *= 3
    }
  }

  private def sampleGlaveTarget(previous: Simulacrum, primary: Simulacrum,
    sourceIsFriendly: Boolean, source: UnitInfo): Simulacrum = {
    val width = With.mapTileWidth
    val tileX = Maff.div32(previous.pixel.x)
    val tileY = Maff.div32(previous.pixel.y)
    val xMin = Math.max(0, tileX - 1)
    val xMax = Math.min(width - 1, tileX + 1)
    val yMin = Math.max(0, tileY - 1)
    val yMax = Math.min(With.mapTileHeight - 1, tileY + 1)
    var total = 0
    var y = yMin
    while (y <= yMax) {
      var x = xMin
      while (x <= xMax) {
        total += tiles(x + y * width).units.size
        x += 1
      }
      y += 1
    }
    if (total == 0) return null
    val random = ThreadLocalRandom.current()
    var attempt = 0
    // Sample storage slots first: dense enemy stacks usually need no per-unit scan.
    while (attempt < 4) {
      var selected = if (total == 1) 0 else random.nextInt(total)
      y = yMin
      var victim: Simulacrum = null
      while (y <= yMax && victim == null) {
        var x = xMin
        while (x <= xMax && victim == null) {
          val units = tiles(x + y * width).units
          if (selected < units.size) victim = units(selected)
          else selected -= units.size
          x += 1
        }
        y += 1
      }
      if (eligibleGlaveTarget(victim, previous, primary, sourceIsFriendly, source)) return victim
      if (total == 1) return null
      attempt += 1
    }
    var chosen: Simulacrum = null
    var candidates = 0
    y = yMin
    // Reservoir fallback bounds work when most units are ineligible and preserves uniform sampling.
    while (y <= yMax) {
      var x = xMin
      while (x <= xMax) {
        val units = tiles(x + y * width).units
        var i = 0
        while (i < units.size) {
          val victim = units(i)
          if (eligibleGlaveTarget(victim, previous, primary, sourceIsFriendly, source)) {
            candidates += 1
            if (candidates == 1 || random.nextInt(candidates) == 0) chosen = victim
          }
          i += 1
        }
        x += 1
      }
      y += 1
    }
    chosen
  }

  @inline private def eligibleGlaveTarget(victim: Simulacrum, previous: Simulacrum, primary: Simulacrum,
    sourceIsFriendly: Boolean, source: UnitInfo): Boolean = (
    (victim ne primary) && (victim ne previous) && (victim.realUnit ne source)
      && victim.alive && victim.hitPoints > 0 && ! victim.invincible && ! victim.stasised
      && victim.isFriendlyCached != sourceIsFriendly
      && (victim.detected || ( ! victim.cloaked && ! victim.burrowed)))

  @inline def remove(unit: Simulacrum): Unit = {
    unit.gridTile.foreach(tile => {
      tile -= unit
      if (tile.units.isEmpty) populatedTiles.remove(tile)
    })
    unit.gridTile = None
  }

  @inline def tryMove(unit: Simulacrum, to: Pixel): Unit = {
    val toClamped = if (to.valid) to else to.clamp()
    if ( ! collisionEnabled || unit.flying || unit.pixel.pixelDistanceSquared(toClamped) <= unit.topSpeed * unit.topSpeed
      || (Maff.div32(unit.pixel.x) == Maff.div32(toClamped.x) && Maff.div32(unit.pixel.y) == Maff.div32(toClamped.y))) {
      val step = unit.pixel.projectUpTo(toClamped, unit.topSpeed)
      val destination = tileAt(step)
      if ( ! collisionEnabled || destination.fits(unit)) {
        relocateUnit(unit, step, destination)
        return
      }
      destination.requestEntry(unit, frame)
    }
    if (tryForceV2(unit, unit.pixel.flowTo(toClamped))){
      return
    }
    if (moveGrid(unit, toClamped)) {
      return
    }
    val fail = "Fail!"
  }

  @inline private def moveGrid(unit: Simulacrum, to: Pixel): Boolean = {
    val fromTile = unit.gridTile.get.tile
    var count = 0
    var direction = 0
    while (direction < 4) {
      val x = fromTile.x + (if (direction == 2) -1 else if (direction == 3) 1 else 0)
      val y = fromTile.y + (if (direction == 0) -1 else if (direction == 1) 1 else 0)
      if (x >= 0 && y >= 0 && x < With.mapTileWidth && y < With.mapTileHeight) {
        val candidate = tiles(x + y * With.mapTileWidth)
        if (candidate.tile.walkable) {
          val cost = candidate.tile.groundTiles(to) + candidate.tile.pixelDistanceSquared(to)
          val backtrack = unit.lastTile.contains(candidate.tile)
          var index = count
          // Stable insertion preserves adjacent4's up/down/left/right tie order.
          while (index > 0 && ((fallbackBacktracks(index - 1) && ! backtrack)
            || (fallbackBacktracks(index - 1) == backtrack && java.lang.Double.compare(cost, fallbackCosts(index - 1)) < 0))) {
            fallbackTiles(index) = fallbackTiles(index - 1)
            fallbackCosts(index) = fallbackCosts(index - 1)
            fallbackBacktracks(index) = fallbackBacktracks(index - 1)
            index -= 1
          }
          fallbackTiles(index) = candidate
          fallbackCosts(index) = cost
          fallbackBacktracks(index) = backtrack
          count += 1
        }
      }
      direction += 1
    }
    var index = 0
    while (index < count) {
      val tile = fallbackTiles(index).tile
      if (tryForceStep(unit, 32 * (tile.x - fromTile.x), 32 * (tile.y - fromTile.y), requestSpace = false)) return true
      index += 1
    }
    false
  }

  @inline def tryYield(unit: Simulacrum): Boolean = {
    if ( ! collisionEnabled || unit.flying || ! unit.canMove || unit.topSpeed <= 0 || ! unit.behavior.fighting || unit.target.isEmpty || unit.gridTile.isEmpty) return false
    val current = unit.gridTile.get
    if ( ! current.requestedLastFrame(unit, frame)) return false
    val target = unit.target.get
    if ( ! target.alive) return false
    val from = unit.pixel
    val goal = unit.moveGoal.getOrElse(target.pixel)
    val forwardX = goal.x - from.x
    val forwardY = goal.y - from.y
    if (forwardX == 0 && forwardY == 0) return false
    val horizontal = Math.abs(forwardX) >= Math.abs(forwardY)
    val preserveRange = unit.inRangeToAttackSimulation(target)
    val range = unit.pixelRangeAgainst(target)
    var bestDirection = -1
    var bestDistance = Double.PositiveInfinity
    var direction = 0
    while (direction < 4) {
      val dx = if (direction == 2) -1 else if (direction == 3) 1 else 0
      val dy = if (direction == 0) -1 else if (direction == 1) 1 else 0
      val x = current.tile.x + dx
      val y = current.tile.y + dy
      val backwards = if (horizontal) dx * forwardX < 0 else dy * forwardY < 0
      if ( ! backwards && x >= 0 && y >= 0 && x < With.mapTileWidth && y < With.mapTileHeight) {
        val destination = tiles(x + y * With.mapTileWidth)
        // Staying in a requested cell costs more than moving toward an unrequested neighbor.
        if ( ! destination.requestedLastFrame(unit, frame) && destination.fits(unit)) {
          val stepX = Maff.clamp(from.x + (dx * unit.topSpeed).toInt, 0, With.mapPixelWidth - 1)
          val stepY = Maff.clamp(from.y + (dy * unit.topSpeed).toInt, 0, With.mapPixelHeight - 1)
          val distance = Maff.broodWarDistanceBox(
            stepX - unit.unitClass.dimensionLeft, stepY - unit.unitClass.dimensionUp,
            stepX + unit.unitClass.dimensionRightExclusive, stepY + unit.unitClass.dimensionDownExclusive,
            target.pixelLeft, target.pixelTop, target.pixelRightExclusive, target.pixelBottomExclusive)
          if ( ! preserveRange || (distance <= range && (unit.pixelRangeMin <= 0 || distance > unit.pixelRangeMin))) {
            val goalDistance = Maff.squared(32 * x + 16 - goal.x) + Maff.squared(32 * y + 16 - goal.y)
            if (goalDistance < bestDistance) {
              bestDistance = goalDistance
              bestDirection = direction
            }
          }
        }
      }
      direction += 1
    }
    if (bestDirection < 0) return false
    val dx = if (bestDirection == 2) -1 else if (bestDirection == 3) 1 else 0
    val dy = if (bestDirection == 0) -1 else if (bestDirection == 1) 1 else 0
    tryForceStep(unit, dx, dy, requestSpace = false)
  }

  @inline private def moveFlow(unit: Simulacrum, to: Pixel): Unit = {

    val from  = unit.pixel
    val force = from.flowTo(to)

    // Choose the rotation direction (+90 or -90) that brings us closer to the goal direction
    lazy val targetRadians = from.radiansTo(to)
    lazy val fRad          = force.radians
    lazy val dPlus         = Math.abs(Maff.radiansTo(fRad + Maff.halfPi, targetRadians))
    lazy val dMinus        = Math.abs(Maff.radiansTo(fRad - Maff.halfPi, targetRadians))
    lazy val direction     = if (dPlus <= dMinus) 1 else -1 // Either -1 or 1; prefer the closer rotation

    if (tryForce(unit, force)) return

    var path = from
      .tile
      .adjacent4
      .filter(_.walkable)
      .sortBy(_.groundTiles(to))
      .find(toTile => tryForce(unit, from.flowTo(toTile.center)))

    if (path.isEmpty) {
      path = None
    }

      /*
    if (tryForce(unit, force.rotate(  45  * direction)))  return
    if (tryForce(unit, force.rotate(- 45  * direction)))  return
    if (tryForce(unit, force.rotate(  90  * direction)))  return
    if (tryForce(unit, force.rotate(- 90  * direction)))  return
    if (tryForce(unit, force.rotate(  135 * direction)))  return
    if (tryForce(unit, force.rotate(- 135 * direction)))  return
    if (tryForce(unit, force.rotate(180)))                return
    */
  }

  @inline private def tileAt(pixel: Pixel): SimulationGridTile = tiles(
    Maff.clamp(Maff.div32(pixel.x), 0, With.mapTileWidth - 1)
      + With.mapTileWidth * Maff.clamp(Maff.div32(pixel.y), 0, With.mapTileHeight - 1))

  @inline def relocateUnit(unit: Simulacrum, to: Pixel, knownTile: SimulationGridTile = null): Unit = {
    val tileFrom  = unit.gridTile
    val tileTo    = if (knownTile == null) tileAt(to) else knownTile

    if ( ! unit.gridTile.contains(tileTo)) {
      unit.gridTile.foreach(tileLast => {
        tileLast -= unit
        if (tileLast.units.isEmpty) {
          populatedTiles.remove(tileLast)
        }
      })
      if (tileTo.units.isEmpty) {
        populatedTiles.add(tileTo)
      }
      tileTo += unit
      unit.lastTile = tileFrom.map(_.tile)
      unit.gridTile = Some(tileTo)
    }
    unit.setPixel(to)
  }

  @inline def tryForceV2(unit: Simulacrum, force: Force, requestSpace: Boolean = true): Boolean =
    tryForceStep(unit, force.x, force.y, requestSpace)

  @inline private def tryForceStep(unit: Simulacrum, forceX: Double, forceY: Double, requestSpace: Boolean): Boolean = {
    val length = Math.sqrt(forceX * forceX + forceY * forceY)
    if (length == 0) return false
    val from = unit.pixel
    val x = Maff.clamp(from.x + (unit.topSpeed * forceX / length).toInt, 0, With.mapPixelWidth - 1)
    val y = Maff.clamp(from.y + (unit.topSpeed * forceY / length).toInt, 0, With.mapPixelHeight - 1)
    if (x == from.x && y == from.y) return false
    val tileStep = tiles(Maff.div32(x) + With.mapTileWidth * Maff.div32(y))
    if ( ! collisionEnabled || tileStep.fits(unit)) {
      relocateUnit(unit, Pixel(x, y), tileStep)
      true
    } else {
      if (requestSpace) tileStep.requestEntry(unit, frame)
      false
    }
  }

  @inline def tryForce(unit: Simulacrum, force: Force): Boolean = {
          val from      = unit.pixel
          val to        = from.add((unit.topSpeed * force.x).toInt, (unit.topSpeed * force.y).toInt).clamp()
          val tile      = to.tile
    lazy  val gridTile  = tiles(tile.i)
          val accept    = ! collisionEnabled || gridTile.fits(unit)
    if (accept) {
      relocateUnit(unit, to)
    }
    accept
  }
}
