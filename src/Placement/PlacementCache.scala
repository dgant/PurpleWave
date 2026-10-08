package Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.{Point, Tile}
import Placement.Access.PlaceLabels
import Placement.Access.PlaceLabels.PlaceLabel
import Placement.Generation.{CannonRushPlacement, Fit}
import Placement.Templating.{Template, TemplatePoint, TemplatePointRequirement}
import Placement.Walls.WallSpans.{TerrainGas, TerrainHall, TerrainTerrain, WallSpan}
import Placement.Walls.{Wall, WallConstraint}
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import bwapi.Race

import java.io.{BufferedInputStream, BufferedOutputStream, DataInputStream, DataOutputStream, File, FileInputStream, FileOutputStream, FilterInputStream, IOException, InputStream}
import java.nio.file.{AtomicMoveNotSupportedException, Files, Paths, StandardCopyOption}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import scala.collection.mutable
import scala.io.Source

object PlacementCache {

  private val Magic = 0x5057504c // PWPL
  private val FormatVersion = 4
  private val MaximumCompressedBytes = 8L * 1024L * 1024L
  private val MaximumDecodedBytes = 32L * 1024L * 1024L
  private val MaximumRequirements = 4096
  private val MaximumFits = 65535
  private val MaximumPointsPerFit = 4096
  private val MaximumTotalPoints = 500000
  private val MaximumWallTiles = 65535

  private val labels: Vector[PlaceLabel] = Vector(
    PlaceLabels.Important,
    PlaceLabels.Unimportant,
    PlaceLabels.TownHall,
    PlaceLabels.MacroHatch,
    PlaceLabels.Gas,
    PlaceLabels.GroundProduction,
    PlaceLabels.Tech,
    PlaceLabels.Supply,
    PlaceLabels.Defensive,
    PlaceLabels.DefendEntrance,
    PlaceLabels.DefendHall,
    PlaceLabels.DefendAir,
    PlaceLabels.DefendGround,
    PlaceLabels.Wall,
    PlaceLabels.AnyProxy,
    PlaceLabels.ProxyGround,
    PlaceLabels.ProxyAir,
    PlaceLabels.Proxy2Rax,
    PlaceLabels.Proxy2Gate,
    PlaceLabels.Proxy4Gate,
    PlaceLabels.ProxyForwardRobo,
    PlaceLabels.CannonRush)
  private val labelIndices: Map[PlaceLabel, Int] = labels.zipWithIndex.toMap
  private val spans: Vector[WallSpan] = Vector(TerrainTerrain, TerrainGas, TerrainHall)

  private case class CacheKey(mapHash: String, ourRace: Int, enemyRace: Int, startX: Int, startY: Int) {
    private def safe(value: String): String = value.replaceAll("[^A-Za-z0-9._-]", "_")
    private def raceName(id: Int): String = Race.values().find(_.id == id).map(_.toString.toLowerCase).getOrElse(id.toString)
    val filename: String = f"placement-${safe(mapHash)}-${raceName(ourRace)}-${raceName(enemyRace)}-$startX-$startY.bin.gz"
  }

  private case class RequirementData(width: Int, height: Int, flags: Int, buildings: Vector[Int], labels: Vector[Int])
  private case class CannonData(base: Tile, cannon: Tile, pylons: Vector[Tile], fitIndex: Int)
  private case class PlacementData(fits: Vector[Fit], forwardProxyNaturals: Vector[Tile], cannonRushes: Vector[CannonData], wall: Option[Wall])

  private sealed trait ReadResult
  private case class Hit(data: PlacementData) extends ReadResult
  private case object Stale extends ReadResult
  private case class Invalid(reason: String) extends ReadResult

  private class CachedRequirement(data: RequirementData, unitClasses: Vector[UnitClass])
    extends TemplatePointRequirement(unitClasses: _*) {
    override val buildableBefore: Boolean = (data.flags & 1) != 0
    override val walkableBefore : Boolean = (data.flags & 2) != 0
    override val buildableAfter : Boolean = (data.flags & 4) != 0
    override val walkableAfter  : Boolean = (data.flags & 8) != 0
    width = data.width
    height = data.height
    labels ++= data.labels.map(PlacementCache.labels)
  }

  private class LimitedInputStream(input: InputStream, maximumBytes: Long) extends FilterInputStream(input) {
    private var remaining = maximumBytes

    override def read(): Int = {
      if (remaining <= 0) throw new IOException("Placement cache expands beyond its size limit")
      val output = super.read()
      if (output >= 0) remaining -= 1
      output
    }

    override def read(buffer: Array[Byte], offset: Int, length: Int): Int = {
      if (remaining <= 0) throw new IOException("Placement cache expands beyond its size limit")
      val output = super.read(buffer, offset, Math.min(length.toLong, remaining).toInt)
      if (output > 0) remaining -= output
      output
    }
  }

  private lazy val revision: Option[String] = {
    val file = Paths.get(With.bwapiData.ai, "revision.txt").toFile
    if ( ! file.isFile) {
      With.logger.warn(f"Placement caching disabled: revision file not found at ${file.getAbsolutePath}")
      None
    } else {
      var source: Source = null
      try {
        source = Source.fromFile(file, "UTF-8")
        val output = source.mkString.trim
        if (output.isEmpty) {
          With.logger.warn(f"Placement caching disabled: revision file is empty at ${file.getAbsolutePath}")
          None
        } else {
          Some(output)
        }
      } catch { case exception: Exception =>
        With.logger.warn(f"Placement caching disabled: failed to read ${file.getAbsolutePath}: ${exception.getMessage}")
        None
      } finally {
        if (source != null) source.close()
      }
    }
  }

  private def key: CacheKey = {
    val start = With.self.startTile
    val enemyRace = With.enemies.headOption.map(_.raceCurrent).getOrElse(Race.None)
    CacheKey(With.game.mapHash, With.self.raceCurrent.id, enemyRace.id, start.x, start.y)
  }

  def load(placement: Placement): Option[Boolean] = {
    if (revision.isEmpty) return None
    val expectedRevision = revision.get
    val expectedKey = key
    val directories = Vector(With.bwapiData.read, With.bwapiData.write, With.bwapiData.ai)
    var rejected = false
    var directoryIndex = 0
    while (directoryIndex < directories.length) {
      val file = Paths.get(directories(directoryIndex), expectedKey.filename).toFile
      if (file.isFile) {
        read(file, expectedRevision, expectedKey) match {
          case Hit(data) =>
            try {
              restore(placement, data)
              placement.finishCachedRestore()
              With.logger.debug(f"Loaded ${data.fits.length} building-placement fits from ${file.getAbsolutePath}")
              return Some(rejected && directoryIndex != 1)
            } catch { case exception: Exception =>
              rejected = true
              With.logger.warn(f"Ignoring invalid building-placement cache ${file.getAbsolutePath}: ${exception.getMessage}")
            }
          case Stale =>
            rejected = true
            With.logger.debug(f"Ignoring building-placement cache from another revision: ${file.getAbsolutePath}")
          case Invalid(reason) =>
            rejected = true
            With.logger.warn(f"Ignoring invalid building-placement cache ${file.getAbsolutePath}: $reason")
        }
      }
      directoryIndex += 1
    }
    None
  }

  def write(placement: Placement): Unit = {
    if (revision.isEmpty) return
    val cacheKey = key
    val directory = Paths.get(With.bwapiData.write)
    val target = directory.resolve(cacheKey.filename)
    var temporary: java.nio.file.Path = null
    try {
      Files.createDirectories(directory)
      temporary = Files.createTempFile(directory, cacheKey.filename + ".", ".tmp")
      write(temporary.toFile, revision.get, cacheKey, placement)
      try {
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
      } catch { case _: AtomicMoveNotSupportedException =>
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
      }
      temporary = null
      With.logger.debug(f"Wrote building-placement cache to ${target.toFile.getAbsolutePath}")
    } catch { case exception: Exception =>
      With.logger.warn(f"Failed to write building-placement cache ${target.toFile.getAbsolutePath}: ${exception.getMessage}")
    } finally {
      if (temporary != null) {
        try Files.deleteIfExists(temporary) catch { case _: Exception => }
      }
    }
  }

  private def restore(placement: Placement, data: PlacementData): Unit = {
    def baseAt(tile: Tile): Base = With.geography.bases.find(_.townHallTile == tile)
      .getOrElse(throw new IOException(f"No base at cached town hall tile $tile"))

    val forwardProxyNaturals = data.forwardProxyNaturals.map(baseAt)
    val cannonRushes = data.cannonRushes.map(cannon => {
      if (cannon.fitIndex < 0 || cannon.fitIndex >= data.fits.length) {
        throw new IOException(f"Invalid cannon-rush fit index ${cannon.fitIndex}")
      }
      CannonRushPlacement(baseAt(cannon.base), cannon.cannon, cannon.pylons, data.fits(cannon.fitIndex))
    })

    data.fits.foreach(placement.index)
    placement.forwardProxyNaturals ++= forwardProxyNaturals
    placement.cannonRushes ++= cannonRushes
    placement.restoreCachedWall(data.wall)
  }

  private def read(file: File, expectedRevision: String, expectedKey: CacheKey): ReadResult = {
    if (file.length() > MaximumCompressedBytes) return Invalid(f"compressed size ${file.length()} exceeds $MaximumCompressedBytes")
    var input: DataInputStream = null
    var fileInput: InputStream = null
    try {
      fileInput = new BufferedInputStream(new FileInputStream(file))
      input = new DataInputStream(new BufferedInputStream(new LimitedInputStream(new GZIPInputStream(fileInput), MaximumDecodedBytes)))
      if (input.readInt() != Magic) return Invalid("incorrect magic")
      if (input.readUnsignedByte() != FormatVersion) return Invalid("unsupported format version")
      if (input.readUTF() != expectedRevision) return Stale
      val actualKey = CacheKey(input.readUTF(), input.readUnsignedByte(), input.readUnsignedByte(), input.readShort(), input.readShort())
      if (actualKey != expectedKey) return Invalid("cache key does not match its filename")

      val requirementCount = count(input.readUnsignedShort(), MaximumRequirements, "requirements")
      val requirements = Vector.fill(requirementCount)(readRequirement(input))
      val fitCount = count(input.readUnsignedShort(), MaximumFits, "fits")
      var totalPoints = 0
      val fits = Vector.fill(fitCount) {
        val origin = Tile(input.readShort(), input.readShort())
        val order = input.readByte().toInt
        val pointCount = count(input.readUnsignedShort(), MaximumPointsPerFit, "points in a fit")
        totalPoints += pointCount
        if (totalPoints > MaximumTotalPoints) throw new IOException(f"Too many placement points: $totalPoints")
        val template = new Template
        var pointIndex = 0
        while (pointIndex < pointCount) {
          val point = Point(input.readShort(), input.readShort())
          val requirementIndex = input.readUnsignedShort()
          if (requirementIndex >= requirements.length) throw new IOException(f"Invalid requirement index $requirementIndex")
          val requirement = requirements(requirementIndex)
          val foundation = origin.add(point)
          if ( ! foundation.valid || ! foundation.add(requirement.width - 1, requirement.height - 1).valid) {
            throw new IOException(f"Invalid cached foundation at $foundation")
          }
          template.points += TemplatePoint(point, requirement)
          pointIndex += 1
        }
        Fit(origin, template, order)
      }

      val forwardProxyNaturals = Vector.fill(count(input.readUnsignedShort(), 256, "forward proxy naturals"))(readTile(input))
      val cannonRushes = Vector.fill(count(input.readUnsignedShort(), 256, "cannon rushes")) {
        val base = readTile(input)
        val cannon = readTile(input)
        val pylons = Vector.fill(count(input.readUnsignedByte(), 32, "cannon-rush pylons"))(readTile(input))
        CannonData(base, cannon, pylons, input.readUnsignedShort())
      }
      val wall = if (input.readBoolean()) Some(readWall(input)) else None
      if (input.read() != -1) throw new IOException("Unexpected data after the placement cache")
      Hit(PlacementData(fits, forwardProxyNaturals, cannonRushes, wall))
    } catch { case exception: Exception =>
      Invalid(Option(exception.getMessage).getOrElse(exception.getClass.getSimpleName))
    } finally {
      if (input != null) {
        try input.close() catch { case _: Exception => }
      } else if (fileInput != null) {
        try fileInput.close() catch { case _: Exception => }
      }
    }
  }

  private def write(file: File, revision: String, cacheKey: CacheKey, placement: Placement): Unit = {
    val fits = placement.fitsIndexOrder
    val requirementIndices = new mutable.LinkedHashMap[RequirementData, Int]
    fits.foreach(_.template.points.foreach(point => {
      val data = requirementData(point.requirement)
      requirementIndices.getOrElseUpdate(data, requirementIndices.size)
    }))
    unsignedShort(requirementIndices.size, "requirements")
    unsignedShort(fits.size, "fits")

    var output: DataOutputStream = null
    try {
      output = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
      output.writeInt(Magic)
      output.writeByte(FormatVersion)
      output.writeUTF(revision)
      output.writeUTF(cacheKey.mapHash)
      output.writeByte(cacheKey.ourRace)
      output.writeByte(cacheKey.enemyRace)
      output.writeShort(cacheKey.startX)
      output.writeShort(cacheKey.startY)

      output.writeShort(requirementIndices.size)
      requirementIndices.keys.foreach(writeRequirement(output, _))
      output.writeShort(fits.size)
      fits.foreach(fit => {
        signedShort(fit.origin.x, "fit origin x")
        signedShort(fit.origin.y, "fit origin y")
        signedByte(fit.order, "fit order")
        unsignedShort(fit.template.points.size, "points in a fit")
        output.writeShort(fit.origin.x)
        output.writeShort(fit.origin.y)
        output.writeByte(fit.order)
        output.writeShort(fit.template.points.size)
        fit.template.points.foreach(point => {
          signedShort(point.point.x, "point x")
          signedShort(point.point.y, "point y")
          output.writeShort(point.point.x)
          output.writeShort(point.point.y)
          output.writeShort(requirementIndices(requirementData(point.requirement)))
        })
      })

      output.writeShort(unsignedShort(placement.forwardProxyNaturals.size, "forward proxy naturals"))
      placement.forwardProxyNaturals.foreach(base => writeTile(output, base.townHallTile))

      val fitIndices = fits.zipWithIndex.toMap
      output.writeShort(unsignedShort(placement.cannonRushes.size, "cannon rushes"))
      placement.cannonRushes.foreach(cannon => {
        writeTile(output, cannon.base.townHallTile)
        writeTile(output, cannon.cannon)
        unsignedByte(cannon.pylons.size, "cannon-rush pylons")
        output.writeByte(cannon.pylons.size)
        cannon.pylons.foreach(writeTile(output, _))
        output.writeShort(unsignedShort(fitIndices.getOrElse(cannon.fit, throw new IOException("Cannon-rush fit was not indexed")), "cannon-rush fit index"))
      })

      output.writeBoolean(placement.wall.isDefined)
      placement.wall.foreach(writeWall(output, _))
    } finally {
      if (output != null) output.close()
    }
  }

  private def requirementData(requirement: TemplatePointRequirement): RequirementData = {
    var flags = 0
    if (requirement.buildableBefore) flags |= 1
    if (requirement.walkableBefore) flags |= 2
    if (requirement.buildableAfter) flags |= 4
    if (requirement.walkableAfter) flags |= 8
    val requirementLabels = requirement.labels.map(label =>
      labelIndices.getOrElse(label, throw new IOException(f"Unknown placement label $label"))).toVector
    RequirementData(requirement.width, requirement.height, flags, requirement.buildings.map(_.id).toVector, requirementLabels)
  }

  private def readRequirement(input: DataInputStream): TemplatePointRequirement = {
    val width = input.readUnsignedByte()
    val height = input.readUnsignedByte()
    if (width <= 0 || width > 16 || height <= 0 || height > 16) throw new IOException(f"Invalid requirement dimensions $width x $height")
    val flags = input.readUnsignedByte()
    if ((flags & ~15) != 0) throw new IOException(f"Invalid requirement flags $flags")
    val buildingCount = count(input.readUnsignedByte(), 64, "buildings in a requirement")
    val buildingIds = Vector.fill(buildingCount)(input.readUnsignedShort())
    val labelCount = count(input.readUnsignedByte(), labels.length, "labels in a requirement")
    val labelIds = Vector.fill(labelCount) {
      val labelId = input.readUnsignedByte()
      if (labelId >= labels.length) throw new IOException(f"Invalid placement label ID $labelId")
      labelId
    }
    val unitClasses = buildingIds.map(unitClass)
    new CachedRequirement(RequirementData(width, height, flags, buildingIds, labelIds), unitClasses)
  }

  private def writeRequirement(output: DataOutputStream, data: RequirementData): Unit = {
    unsignedByte(data.width, "requirement width")
    unsignedByte(data.height, "requirement height")
    unsignedByte(data.buildings.size, "buildings in a requirement")
    output.writeByte(data.width)
    output.writeByte(data.height)
    output.writeByte(data.flags)
    output.writeByte(data.buildings.size)
    data.buildings.foreach(id => output.writeShort(unsignedShort(id, "unit type ID")))
    output.writeByte(unsignedByte(data.labels.size, "labels in a requirement"))
    data.labels.foreach(label => output.writeByte(unsignedByte(label, "placement label ID")))
  }

  private def readWall(input: DataInputStream): Wall = {
    val wall = new Wall
    val gapTiles = input.readUnsignedByte()
    val blocksUnit = unitClass(input.readUnsignedShort())
    val spanIndex = input.readUnsignedByte()
    if (spanIndex >= spans.length) throw new IOException(f"Invalid wall span index $spanIndex")
    val constraintBuildings = Vector.fill(count(input.readUnsignedByte(), 64, "wall constraint buildings"))(unitClass(input.readUnsignedShort()))
    wall.constraint = WallConstraint(gapTiles, blocksUnit, spans(spanIndex), constraintBuildings: _*)
    val buildingCount = count(input.readUnsignedShort(), 256, "wall buildings")
    wall.buildings ++= Vector.fill(buildingCount)((readTile(input), unitClass(input.readUnsignedShort())))
    wall.gap = if (input.readBoolean()) Some(readTile(input)) else None
    wall.hallway = Vector.fill(count(input.readUnsignedShort(), MaximumWallTiles, "wall hallway"))(readTile(input))
    wall.hallwayOutside = Vector.fill(count(input.readUnsignedShort(), MaximumWallTiles, "outside wall hallway"))(readTile(input))
    wall.score = input.readDouble()
    wall
  }

  private def writeWall(output: DataOutputStream, wall: Wall): Unit = {
    val spanIndex = spans.indexOf(wall.constraint.span)
    if (spanIndex < 0) throw new IOException(f"Unknown wall span ${wall.constraint.span}")
    unsignedByte(wall.constraint.gapTiles, "wall gap tiles")
    unsignedByte(wall.constraint.buildings.size, "wall constraint buildings")
    output.writeByte(wall.constraint.gapTiles)
    output.writeShort(unsignedShort(wall.constraint.blocksUnit.id, "wall blocking unit ID"))
    output.writeByte(spanIndex)
    output.writeByte(wall.constraint.buildings.size)
    wall.constraint.buildings.foreach(building => output.writeShort(unsignedShort(building.id, "wall constraint unit ID")))
    output.writeShort(unsignedShort(wall.buildings.size, "wall buildings"))
    wall.buildings.foreach(building => {
      writeTile(output, building._1)
      output.writeShort(unsignedShort(building._2.id, "wall building unit ID"))
    })
    output.writeBoolean(wall.gap.isDefined)
    wall.gap.foreach(writeTile(output, _))
    output.writeShort(unsignedShort(wall.hallway.size, "wall hallway"))
    wall.hallway.foreach(writeTile(output, _))
    output.writeShort(unsignedShort(wall.hallwayOutside.size, "outside wall hallway"))
    wall.hallwayOutside.foreach(writeTile(output, _))
    output.writeDouble(wall.score)
  }

  private def readTile(input: DataInputStream): Tile = {
    val tile = Tile(input.readShort(), input.readShort())
    if ( ! tile.valid) throw new IOException(f"Invalid cached tile $tile")
    tile
  }

  private def writeTile(output: DataOutputStream, tile: Tile): Unit = {
    if ( ! tile.valid) throw new IOException(f"Attempted to cache invalid tile $tile")
    output.writeShort(tile.x)
    output.writeShort(tile.y)
  }

  private def unitClass(id: Int): UnitClass = {
    if (id < 0 || id >= With.proxy.unitClassesById.length || With.proxy.unitClassesById(id).id != id) {
      throw new IOException(f"Invalid unit type ID $id")
    }
    UnitClasses.get(With.proxy.unitClassesById(id).bwapiType)
  }

  private def count(value: Int, maximum: Int, name: String): Int = {
    if (value < 0 || value > maximum) throw new IOException(f"Invalid $name count $value")
    value
  }

  private def unsignedByte(value: Int, name: String): Int = {
    if (value < 0 || value > 255) throw new IOException(f"$name $value does not fit in an unsigned byte")
    value
  }

  private def signedByte(value: Int, name: String): Int = {
    if (value < Byte.MinValue || value > Byte.MaxValue) throw new IOException(f"$name $value does not fit in a signed byte")
    value
  }

  private def unsignedShort(value: Int, name: String): Int = {
    if (value < 0 || value > 65535) throw new IOException(f"$name $value does not fit in an unsigned short")
    value
  }

  private def signedShort(value: Int, name: String): Int = {
    if (value < Short.MinValue || value > Short.MaxValue) throw new IOException(f"$name $value does not fit in a signed short")
    value
  }
}
