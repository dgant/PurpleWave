package Debugging

import mjson.Json

import java.awt.image.BufferedImage
import java.awt._
import java.io.{DataInputStream, File, FileInputStream, RandomAccessFile}
import java.util.zip.GZIPInputStream
import javax.swing._
import scala.util.Try

/**
  * Combat simulation viewer with scaling, playback controls, and auto-reload toggle.
  * Usage: Debugging.CombatVisualizer <path-to-simulation.pwcs>
  * If no path is provided, defaults to simulation.pwcs in the working directory (falls back to simulation.json).
  */
object PurpleSimViz {

  // External diagnostics to c:\p\pw\out\logs for robust investigation
  private def extVisLog(message: String): Unit = {
    try {
      val dir = new java.io.File("C:\\p\\pw\\out\\logs")
      if (!dir.exists()) dir.mkdirs()
      val ts = System.currentTimeMillis()
      val sdf = new java.text.SimpleDateFormat("HH:mm:ss.SSS")
      val line = s"[$ts ${sdf.format(new java.util.Date(ts))}] $message\n"
      val f = new java.io.File(dir, "combatvis.log")
      val fos = new java.io.FileOutputStream(f, true)
      try fos.write(line.getBytes(java.nio.charset.StandardCharsets.UTF_8)) finally try fos.close() catch { case _: Throwable => }
    } catch { case _: Throwable => }
  }

  // Stderr logger for immediate user diagnostics
  private def err(message: String): Unit = {
    try {
      System.err.println("[PurpleSimViz] " + message)
    } catch { case _: Throwable => () }
  }

  @volatile private var window: JFrame = _
  @volatile private var timerRef: javax.swing.Timer = _
  // Directory of the current simulation file (used to find images)
  @volatile private var currentSimDir: File = new File(".")
  def requestClose(): Unit = {
    try {
      SwingUtilities.invokeLater(new Runnable { override def run(): Unit = {
        try { if (timerRef != null) timerRef.stop() } catch { case _: Throwable => }
        try { if (window != null) window.dispose() } catch { case _: Throwable => }
      }})
    } catch { case _: Throwable => }
  }

  case class UnitMeta(id: Int, friendly: Boolean, width: Int, height: Int, unitType: String, fly: Boolean = false)
  case class UnitFrame(id: Int, friendly: Boolean, x: Int, y: Int, alive: Boolean, width: Int, height: Int, hp: Int = -1, sh: Int = -1, hpMax: Int = -1, shMax: Int = -1, tgt: Option[Int] = None, fly: Boolean = false, cd: Int = -1)
  case class FrameData(f: Int, units: Vector[UnitFrame], attacks: Vector[(Int, Int)] = Vector.empty, deaths: Vector[Int] = Vector.empty)

  final class MapData(val tileW: Int, val tileH: Int, val walkW: Int, val walkH: Int,
                      val tileHeights: Array[Byte], val walkable: Array[Byte]) {
    val pixelW: Int = tileW * 32
    val pixelH: Int = tileH * 32

    // Pre-render to image for fast painting
    lazy val image: BufferedImage = {
      val img = new BufferedImage(pixelW, pixelH, BufferedImage.TYPE_INT_RGB)
      val g = img.getGraphics
      try {
        // Render tiles as 32x32 blocks with grayscale from height
        var ty = 0
        while (ty < tileH) {
          var tx = 0
          while (tx < tileW) {
            val idx = ty * tileW + tx
            val h = (tileHeights(idx) & 0xFF)
            val gray = Math.min(255, 40 + h * 10) // heuristic scaling
            g.setColor(new Color(gray, gray, gray))
            g.fillRect(tx * 32, ty * 32, 32, 32)
            tx += 1
          }
          ty += 1
        }
        // Darken unwalkable mini-tiles
        var wy = 0
        while (wy < walkH) {
          var wx = 0
          while (wx < walkW) {
            val widx = wy * walkW + wx
            val walk = (walkable(widx) & 0xFF) != 0
            if (!walk) {
              val px = (wx * 8)
              val py = (wy * 8)
              g.setColor(new Color(0, 0, 0))
              g.fillRect(px, py, 8, 8)
            }
            wx += 1
          }
          wy += 1
        }
      } finally {
        g.dispose()
      }
      img
    }
  }

  private def loadMap(mapDir: File, mapHash: String): Option[MapData] = {
    val f = new File(mapDir, s"$mapHash.mapbin")
    if (!f.exists()) return None
    Try {
      val dis = new DataInputStream(new FileInputStream(f))
      try {
        val tileW = dis.readInt()
        val tileH = dis.readInt()
        val walkW = dis.readInt()
        val walkH = dis.readInt()
        val tileCount = tileW * tileH
        val walkCount = walkW * walkH
        val heights = new Array[Byte](tileCount)
        dis.readFully(heights)
        val walkable = new Array[Byte](walkCount)
        dis.readFully(walkable)
        new MapData(tileW, tileH, walkW, walkH, heights, walkable)
      } finally dis.close()
    }.toOption
  }

  // Safe JSON accessors (mjson can return nulls; guard them carefully)
  private def jAt(j: Json, key: String): Json = {
    if (j == null) null.asInstanceOf[Json]
    else try j.at(key) catch { case _: Throwable => null.asInstanceOf[Json] }
  }
  private def getIntOpt(j: Json, key: String): Option[Int] = {
    val n = jAt(j, key)
    if (n == null) None
    else try {
      val v: java.lang.Integer = n.asInteger()
      if (v == null) None else Some(v.intValue())
    } catch { case _: Throwable => None }
  }
  private def getBoolOpt(j: Json, key: String): Option[Boolean] = {
    val n = jAt(j, key)
    if (n == null) None
    else try {
      val v: java.lang.Boolean = n.asBoolean()
      if (v == null) None else Some(v.booleanValue())
    } catch { case _: Throwable => None }
  }
  private def getStringOpt(j: Json, key: String): Option[String] = {
    val n = jAt(j, key)
    if (n == null) None
    else try {
      val v: String = n.asString()
      Option(v)
    } catch { case _: Throwable => None }
  }
  private def getInt(j: Json, key: String, default: Int): Int = getIntOpt(j, key).getOrElse(default)
  private def getBool(j: Json, key: String, default: Boolean): Boolean = getBoolOpt(j, key).getOrElse(default)
  private def getString(j: Json, key: String, default: String): String = getStringOpt(j, key).getOrElse(default)

  case class SimPack(mapHash: String, metas: Vector[UnitMeta], frames: Vector[FrameData], startGameFrame: Int)

  private def parseSimulation(jsonFile: File): Option[SimPack] = {
    if (!jsonFile.exists()) return None
    Try {
      def readAll(f: File): String = {
        var fis: java.io.FileInputStream = null
        var bis: java.io.BufferedInputStream = null
        try {
          fis = new FileInputStream(f)
          bis = new java.io.BufferedInputStream(fis)
          bis.mark(4)
          val b1 = bis.read()
          val b2 = bis.read()
          bis.reset()
          val isGz = (b1 == 0x1f && b2 == 0x8b)
          val in: java.io.InputStream = if (isGz) new GZIPInputStream(bis) else bis
          try scala.io.Source.fromInputStream(in)(scala.io.Codec.UTF8).mkString finally in.close()
        } finally {
          try if (bis != null) bis.close() catch { case _: Throwable => }
          try if (fis != null) fis.close() catch { case _: Throwable => }
        }
      }
      val txt = readAll(jsonFile)
      if (txt.startsWith("PW2\n") || txt.startsWith("PW2\r\n")) {
        // Support multi-simulation files: select the newest complete block ending with Z
        val blocks = {
          val lines = txt.split("\r?\n")
          val out = new scala.collection.mutable.ArrayBuffer[String]()
          var i = 0
          var cur = new StringBuilder()
          var in = false
          while (i < lines.length) {
            val line = lines(i)
            val lt = if (line == null) "" else line.trim
            if (lt == "PW2") {
              if (in) {
                // previous block without terminator: drop it
                cur = new StringBuilder()
              }
              in = true
              cur.append("PW2\n")
            } else if (in) {
              cur.append(line).append('\n')
              if (lt == "Z") {
                out += cur.toString()
                cur = new StringBuilder()
                in = false
              }
            }
            i += 1
          }
          out.toVector
        }
        if (blocks.nonEmpty) parsePW2(blocks.last) else parsePW2(txt) // fallback to whole text
      } else parseJsonSim(txt)
    }.toOption
  }

  private def parseJsonSim(txt: String): SimPack = {
    val j = Json.read(txt)
    val mapHash = getString(j, "mapHash", "")
    val unitsJ = Option(j.at("units")).map(_.asJsonList()).map(_.toArray(new Array[Json](0)).toVector).getOrElse(Vector.empty)
    val metas = unitsJ.flatMap { u =>
      val idOpt = getIntOpt(u, "id")
      idOpt.map { id =>
        UnitMeta(
          id = id,
          friendly = getBool(u, "friendly", default = false),
          width = getInt(u, "width", default = 16),
          height = getInt(u, "height", default = 16),
          unitType = getString(u, "type", default = ""),
          fly = getBool(u, "fly", default = false)
        )
      }
    }
    val metaById: Map[Int, UnitMeta] = metas.map(m => m.id -> m).toMap
    val framesJ = Option(j.at("frames")).map(_.asJsonList()).map(_.toArray(new Array[Json](0)).toVector).getOrElse(Vector.empty)
    val frames = framesJ.flatMap { fj =>
      val fnum = getInt(fj, "f", 0)
      val unitsArr = Option(fj.at("u")).map(_.asJsonList()).map(_.toArray(new Array[Json](0)).toVector).getOrElse(Vector.empty)
      val units = unitsArr.flatMap { uj =>
        val idOpt = getIntOpt(uj, "id")
        idOpt.flatMap { id =>
          val xOpt = getIntOpt(uj, "x")
          val yOpt = getIntOpt(uj, "y")
          val aliveOpt = getBoolOpt(uj, "alive")
          if (xOpt.isEmpty || yOpt.isEmpty || aliveOpt.isEmpty) None
          else {
            val meta: UnitMeta = metaById.getOrElse(id, UnitMeta(
              id,
              friendly = getBool(uj, "friendly", default = true),
              width    = getInt(uj, "width", default = 16),
              height   = getInt(uj, "height", default = 16),
              unitType = getString(uj, "type", default = ""),
              fly      = getBool(uj, "fly", default = false)
            ))
            val fr  = getBool(uj, "friendly", default = meta.friendly)
            val w   = getInt(uj, "width", default = meta.width)
            val h   = getInt(uj, "height", default = meta.height)
            val hp    = getIntOpt(uj, "hp").getOrElse(-1)
            val sh    = getIntOpt(uj, "sh").getOrElse(-1)
            val hpMax = getIntOpt(uj, "hpMax").getOrElse(-1)
            val shMax = getIntOpt(uj, "shMax").getOrElse(-1)
            val tgt   = getIntOpt(uj, "tgt")
            val fly   = getBool(uj, "fly", default = meta.fly)
            val cd    = getIntOpt(uj, "cd").getOrElse(-1)
            Some(UnitFrame(id, fr, xOpt.get, yOpt.get, aliveOpt.get, w, h, hp, sh, hpMax, shMax, tgt, fly, cd))
          }
        }
      }
      val attacksArr = Option(fj.at("a")).map(_.asJsonList()).map(_.toArray(new Array[Json](0)).toVector).getOrElse(Vector.empty)
      val attacks = attacksArr.flatMap { pair =>
        if (pair.isArray) {
          val arr = pair.asJsonList()
          Try((arr.get(0).asInteger(), arr.get(1).asInteger())).toOption
        } else None
      }
      val deathsArr = Option(fj.at("d")).map(_.asJsonList()).map(_.toArray(new Array[Json](0)).toVector).getOrElse(Vector.empty)
      val deaths = deathsArr.flatMap(jv => Try(jv.asInteger()).toOption)
      Some(FrameData(fnum, units, attacks, deaths))
    }
    SimPack(mapHash, metas, frames, 0)
  }

  private def parsePW2(txt: String): SimPack = {
    val lines = txt.split("\r?\n").toVector
    var i = 0
    var mapHash = ""
    val metasBuf = new scala.collection.mutable.ArrayBuffer[UnitMeta]()
    val metaById = new scala.collection.mutable.HashMap[Int, UnitMeta]()
    // We'll derive hpMax/shMax and geometry from JBWAPI UnitType via typeId from U lines
    case class St(var x: Int, var y: Int, var alive: Boolean, var hp: Int, var sh: Int, var tgt: Option[Int], var cd: Int)
    val last = new scala.collection.mutable.HashMap[Int, St]()
    val framesBuf = new scala.collection.mutable.ArrayBuffer[FrameData]()

    var currentFrameNum = 0
    var startGameFrame = 0
    def snapshotUnits(): Vector[UnitFrame] = {
      val out = new scala.collection.mutable.ArrayBuffer[UnitFrame]()
      val it = last.iterator
      while (it.hasNext) {
        val (id, st) = it.next()
        if (st.alive) {
          val m = metaById.getOrElse(id, UnitMeta(id, friendly = false, 16, 16, "", fly = false))
          // Derive hp/shield maxima from unit type via JBWAPI (based on resolved unitType in meta)
          val utOpt = try Option(bwapi.UnitType.valueOf(m.unitType)) catch { case _: Throwable => None }
          val hpM = utOpt.map(_.maxHitPoints()).getOrElse(-1)
          val shM = utOpt.map(_.maxShields()).getOrElse(0)
          out += UnitFrame(id, m.friendly, st.x, st.y, st.alive, m.width, m.height, st.hp, st.sh, hpM, shM, st.tgt, m.fly, st.cd)
        }
      }
      out.toVector
    }

    i = 0
    while (i < lines.length) {
      val line = lines(i)
      if (line.startsWith("H|")) {
        mapHash = line.substring(2)
      } else if (line.startsWith("U|")) {
        val f = line.substring(2).split("\\|")
        if (f.length >= 3) {
          val id = Try(f(0).toInt).getOrElse(-1)
          if (id >= 0) {
            val fr = f(1) == "1"
            if (f.length == 3) {
              // New compact format: id|friendly|typeId
              val typeId = Try(f(2).toInt).getOrElse(-1)
              val utOpt = bwapi.UnitType.values().find(_.id == typeId)
              utOpt.foreach { ut =>
                val flyInf = if (ut.getRace() == bwapi.Race.Terran && ut.isBuilding()) false else ut.isFlyer()
                val meta = UnitMeta(id, fr, ut.width(), ut.height(), ut.toString, flyInf)
                metasBuf += meta
                metaById.put(id, meta)
              }
            } else if (f.length >= 8) {
              // Legacy extended format: preserve behavior
              val w = Try(f(2).toInt).getOrElse(16)
              val h = Try(f(3).toInt).getOrElse(16)
              val tpe = if (f(4) == null) "" else f(4)
              val fly = f(5) == "1"
              val meta = UnitMeta(id, fr, w, h, tpe, fly)
              metasBuf += meta
              metaById.put(id, meta)
            }
          }
        }
      } else if (line.startsWith("S|")) {
        startGameFrame = Try(line.substring(2).toInt).getOrElse(0)
      } else if (line.startsWith("F|")) {
        currentFrameNum = Try(line.substring(2).toInt).getOrElse(0)
        // Start a new frame; we'll add after processing following lines (C/A/D) or immediately if none
        // Ensure we at least append a frame with current snapshot at the F line to match previous behavior
        // We'll delay adding until we see next F or EOF to include events collected below
        // So do nothing here
      } else if (line.startsWith("C|")) {
        val body = line.substring(2)
        if (body.nonEmpty) {
          val ents = body.split(';')
          var ei = 0
          while (ei < ents.length) {
            val e = ents(ei)
            val ff = e.split(',')
            if (ff.length >= 8) {
              val id = Try(ff(0).toInt).getOrElse(-1)
              if (id >= 0) {
                val x = Try(ff(1).toInt).getOrElse(0)
                val y = Try(ff(2).toInt).getOrElse(0)
                val alive = ff(3) == "1"
                val hp = Try(ff(4).toInt).getOrElse(-1)
                val sh = Try(ff(5).toInt).getOrElse(-1)
                val tgt = Try(ff(6).toInt).toOption
                val cd = Try(ff(7).toInt).getOrElse(-1)
                val st = last.getOrElseUpdate(id, St(x, y, alive, hp, sh, None, -1))
                st.x = x; st.y = y; st.alive = alive; st.hp = hp; st.sh = sh; st.tgt = tgt.filter(_ >= 0); st.cd = cd
              }
            }
            ei += 1
          }
        }
        // After applying changes for this frame, create a snapshot frame (we'll attach events possibly in following lines by separate data structure)
        framesBuf += FrameData(currentFrameNum, snapshotUnits(), Vector.empty, Vector.empty)
      } else if (line.startsWith("A|")) {
        val body = line.substring(2)
        // Ensure a snapshot exists for this frame even if no C| was recorded
        if (framesBuf.isEmpty || framesBuf(framesBuf.length - 1).f != currentFrameNum) {
          framesBuf += FrameData(currentFrameNum, snapshotUnits(), Vector.empty, Vector.empty)
        }
        if (framesBuf.nonEmpty) {
          val lastF = framesBuf(framesBuf.length - 1)
          val pairs = new scala.collection.mutable.ArrayBuffer[(Int, Int)]()
          val ents = if (body.isEmpty) Array.empty[String] else body.split(';')
          var k = 0
          while (k < ents.length) {
            val s = ents(k)
            val gt = s.indexOf('>')
            if (gt > 0) {
              val a = Try(s.substring(0, gt).toInt).getOrElse(-1)
              val v = Try(s.substring(gt + 1).toInt).getOrElse(-1)
              if (a >= 0 && v >= 0) pairs += ((a, v))
            }
            k += 1
          }
          framesBuf(framesBuf.length - 1) = lastF.copy(attacks = pairs.toVector)
        }
      } else if (line.startsWith("D|")) {
        val body = line.substring(2)
        // Ensure a snapshot exists for this frame even if no C| was recorded
        if (framesBuf.isEmpty || framesBuf(framesBuf.length - 1).f != currentFrameNum) {
          framesBuf += FrameData(currentFrameNum, snapshotUnits(), Vector.empty, Vector.empty)
        }
        if (framesBuf.nonEmpty) {
          val lastF = framesBuf(framesBuf.length - 1)
          val ids = new scala.collection.mutable.ArrayBuffer[Int]()
          val ents = if (body.isEmpty) Array.empty[String] else body.split(';')
          var k = 0
          while (k < ents.length) {
            val s = ents(k)
            val id = Try(s.toInt).getOrElse(-1)
            if (id >= 0) ids += id
            k += 1
          }
          framesBuf(framesBuf.length - 1) = lastF.copy(deaths = ids.toVector)
        }
      }
      i += 1
    }

    SimPack(mapHash, metasBuf.toVector, framesBuf.toVector, startGameFrame)
  }

  // Image loading/cache
  private val imageCache = new java.util.concurrent.ConcurrentHashMap[String, java.awt.Image]()
  private val missingCache = new java.util.concurrent.ConcurrentHashMap[String, java.lang.Boolean]()
  // Cache for recolored (team-tinted) images: key = name + "|F" or "|E"
  private val recolorCache = new java.util.concurrent.ConcurrentHashMap[String, java.awt.Image]()
  private def candidateImageDirs(): Vector[File] = {
    val dirs = new scala.collection.mutable.ArrayBuffer[File]()

    def addIfValid(base: File): Unit = {
      if (base != null) {
        var cur: File = base.getAbsoluteFile
        var depth = 0
        while (cur != null && depth < 8) {
          val f = new File(cur, "images" + File.separator + "units")
          if (f.exists() && f.isDirectory) dirs += f
          cur = cur.getParentFile
          depth += 1
        }
      }
    }

    // Try from simulation file directory, current working dir, and user.dir
    addIfValid(currentSimDir)
    addIfValid(new File("."))
    addIfValid(new File(System.getProperty("user.dir", ".")))

    // Also try alongside the code location (jar/class directory)
    try {
      val url = PurpleSimViz.getClass.getProtectionDomain.getCodeSource.getLocation
      if (url != null) addIfValid(new File(url.toURI))
    } catch { case _: Throwable => }

    dirs.distinct.toVector
  }
  private def getUnitImage(name: String): Option[java.awt.Image] = {
    if (name == null || name.isEmpty) return None
    // Try several filename variants to match repository naming conventions
    val variants = Vector(
      name,
      name.replace(' ', '_'),
      name.replace('_', ' ')
    ).distinct
    // Return cached if any variant cached
    variants.foreach { v =>
      val c = imageCache.get(v)
      if (c != null) return Some(c)
    }
    // If any variant is known missing, still try others
    val dirs = candidateImageDirs()
    var img: java.awt.Image = null
    var cachedKey: String = null
    var i = 0
    while (i < dirs.length && img == null) {
      var vi = 0
      while (vi < variants.length && img == null) {
        val key = variants(vi)
        val f = new File(dirs(i), key + ".png")
        if (f.exists()) {
          try {
            img = javax.imageio.ImageIO.read(f)
            cachedKey = key
          } catch { case _: Throwable => img = null }
        }
        vi += 1
      }
      i += 1
    }
    if (img != null) {
      imageCache.put(cachedKey, img)
      Some(img)
    } else {
      variants.foreach(v => missingCache.put(v, true))
      None
    }
  }

  // Return a team-recolored version of the unit image where saturated pink pixels are remapped
  // to blue (friendly) or red (enemy), preserving brightness. Cached per (name, team).
  private def getRecoloredUnitImage(name: String, friendly: Boolean): Option[java.awt.Image] = {
    if (name == null || name.isEmpty) return None
    val cacheKey = name + (if (friendly) "|F" else "|E")
    val cached = recolorCache.get(cacheKey)
    if (cached != null) return Some(cached)
    val baseOpt = getUnitImage(name)
    baseOpt.flatMap { base =>
      try {
        val bw = base.getWidth(null)
        val bh = base.getHeight(null)
        val src = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_ARGB)
        val g = src.getGraphics
        try { g.drawImage(base, 0, 0, null) } finally { g.dispose() }
        val out = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_ARGB)
        var y = 0
        while (y < bh) {
          var x = 0
          while (x < bw) {
            val argb = src.getRGB(x, y)
            val a = (argb >>> 24) & 0xFF
            val r = (argb >>> 16) & 0xFF
            val gch = (argb >>> 8) & 0xFF
            val b = argb & 0xFF
            if (a == 0) {
              out.setRGB(x, y, argb)
            } else if (isTeamPink(r, gch, b)) {
              // Use the original brightness to set the chosen channel
              val intensity = Math.max(r, b)
              val nr = if (friendly) 0 else intensity
              val ng = 0
              val nb = if (friendly) intensity else 0
              val newArgb = (a << 24) | (nr << 16) | (ng << 8) | nb
              out.setRGB(x, y, newArgb)
            } else {
              out.setRGB(x, y, argb)
            }
            x += 1
          }
          y += 1
        }
        recolorCache.put(cacheKey, out)
        Some(out)
      } catch { case _: Throwable => None }
    }
  }

  // Heuristic to detect saturated pink placeholder pixels in art
  private def isTeamPink(r: Int, g: Int, b: Int): Boolean = {
    // Very saturated pink/magenta: R and B high, G low; allow varying brightness
    val max = Math.max(r, Math.max(g, b))
    val min = Math.min(r, Math.min(g, b))
    val sat = if (max == 0) 0.0 else (max - min).toDouble / max.toDouble
    val brightEnough = max >= 60
    val rbHigh = r >= 180 && b >= 180
    val glow = g <= 80
    (rbHigh && glow) || (brightEnough && sat >= 0.6 && r > g + 60 && b > g + 60)
  }

  private class ViewPanel(var map: Option[MapData], var frames: Vector[FrameData]) extends JPanel {
    setPreferredSize(new Dimension(map.map(_.pixelW).getOrElse(800), map.map(_.pixelH).getOrElse(600)))
    setDoubleBuffered(true)

    // Playback state
    @volatile var frameIndex = 0

    // Metas and cooldown maxima (set from main on load/reload)
    @volatile var metas: Vector[UnitMeta] = Vector.empty
    @volatile var cdMaxById: Map[Int, Int] = Map.empty

    // Fallback world size when map is not available
    @volatile var worldW: Int = 0
    @volatile var worldH: Int = 0

    // Auto-zoom state
    @volatile var autoZoomEnabled: Boolean = false
    // zoomRect: (x, y, w, h) in world pixels (pre-zoom, including margins)
    @volatile var zoomRect: Option[(Int, Int, Int, Int)] = None

    private val margin: Int = 64 // pixels (pre-zoom)

    def currentFrameData: Option[FrameData] = if (frames.isEmpty) None else Some(frames(Math.max(0, Math.min(frameIndex, frames.length - 1))))

    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)
      val g2 = g.asInstanceOf[java.awt.Graphics2D]
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
      g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

      val w = getWidth
      val h = getHeight

      // Determine base world rect (either full world or auto-zoom bounds), then adjust to panel aspect to fill area
      val (baseX, baseY, worldWidth, worldHeight) = {
        val fullW = map.map(_.pixelW).getOrElse(if (worldW > 0) worldW else 1024)
        val fullH = map.map(_.pixelH).getOrElse(if (worldH > 0) worldH else 768)
        val init = if (autoZoomEnabled) zoomRect.getOrElse((0, 0, fullW, fullH)) else (0, 0, fullW, fullH)
        var ax = init._1; var ay = init._2; var aw = Math.max(1, init._3); var ah = Math.max(1, init._4)
        val panelAR = if (h > 0) w.toDouble / h.toDouble else 1.0
        val rectAR = aw.toDouble / ah.toDouble
        if (Math.abs(rectAR - panelAR) > 1e-6) {
          if (rectAR < panelAR) {
            // Too tall: need to expand width
            val desiredW = Math.max(1, (ah * panelAR).toInt)
            val delta = desiredW - aw
            ax -= delta / 2
            aw = desiredW
          } else {
            // Too wide: need to expand height
            val desiredH = Math.max(1, (aw / panelAR).toInt)
            val delta = desiredH - ah
            ay -= delta / 2
            ah = desiredH
          }
          // Clamp to map/world bounds
          if (ax < 0) ax = 0
          if (ay < 0) ay = 0
          if (ax + aw > fullW) ax = Math.max(0, fullW - aw)
          if (ay + ah > fullH) ay = Math.max(0, fullH - ah)
        }
        (ax, ay, Math.max(1, Math.min(aw, fullW)), Math.max(1, Math.min(ah, fullH)))
      }

      val scale = Math.min(w.toDouble / worldWidth, h.toDouble / worldHeight)
      val drawW = Math.max(1, (worldWidth * scale).toInt)
      val drawH = Math.max(1, (worldHeight * scale).toInt)
      val offX = (w - drawW) / 2
      val offY = (h - drawH) / 2

      // Draw terrain or fallback background
      map match {
        case Some(m) =>
          if (autoZoomEnabled) {
            // Clamp source rect to map bounds
            val sx1 = Math.max(0, baseX)
            val sy1 = Math.max(0, baseY)
            val sx2 = Math.min(m.pixelW, baseX + worldWidth)
            val sy2 = Math.min(m.pixelH, baseY + worldHeight)
            g2.drawImage(m.image, offX, offY, offX + drawW, offY + drawH, sx1, sy1, sx2, sy2, null)
          } else {
            g2.drawImage(m.image, offX, offY, drawW, drawH, null)
          }
        case None =>
          g2.setColor(new Color(32, 32, 32))
          g2.fillRect(offX, offY, drawW, drawH)
      }

      // Draw units and events for current frame
      currentFrameData.foreach { fd =>
        // Build a lookup map for positions by id
        val posById = fd.units.map(u => u.id -> u).toMap
        val blue = new Color(0, 0, 255)
        val red = new Color(255, 0, 0)
        val blueDark = blue.darker()
        val redDark = red.darker()
        val pink = new Color(255, 105, 180)
        // Draw target lines (darker team color)
        fd.units.foreach { u =>
          u.tgt.foreach { tid =>
            posById.get(tid).foreach { tv =>
              if (u.alive) {
                val sx = (offX + (u.x - baseX) * scale).toInt
                val sy = (offY + (u.y - baseY) * scale).toInt
                val tx = (offX + (tv.x - baseX) * scale).toInt
                val ty = (offY + (tv.y - baseY) * scale).toInt
                g2.setColor(if (u.friendly) new Color(blueDark.getRed, blueDark.getGreen, blueDark.getBlue, 180) else new Color(redDark.getRed, redDark.getGreen, redDark.getBlue, 180))
                g2.drawLine(sx, sy, tx, ty)
              }
            }
          }
        }
        // First draw units with health fill and outline
        val metaById: Map[Int, UnitMeta] = metas.map(m => m.id -> m).toMap
        fd.units.foreach { u =>
          if (u.alive) {
            val color = if (u.friendly) blue else red
            // Health fraction
            val maxTotal = Math.max(0, u.hpMax) + Math.max(0, u.shMax)
            val curTotal = Math.max(0, u.hp) + Math.max(0, u.sh)
            val frac = if (maxTotal > 0) Math.max(0.0, Math.min(1.0, curTotal.toDouble / maxTotal)) else 1.0
            if (u.fly) {
              // Draw circle with clipped health fill
              val cx = offX + (u.x - baseX) * scale
              val cy = offY + (u.y - baseY) * scale
              val diam = Math.max(u.width, u.height) * scale
              val r = diam / 2.0
              val ellipse = new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, diam, diam)
              val oldClip = g2.getClip
              g2.setClip(ellipse)
              g2.setColor(if (u.friendly) new Color(0, 0, 255, 140) else new Color(255, 0, 0, 140))
              val filled = Math.max(1, (diam * frac).toInt)
              g2.fillRect((cx - r).toInt, (cy + r - filled).toInt, Math.max(1, diam.toInt), filled)
              g2.setClip(oldClip)
              g2.setColor(color)
              g2.draw(ellipse)
              // Draw unit image centered over circle, at intrinsic image size scaled by global scale
              metaById.get(u.id).flatMap(m => getRecoloredUnitImage(m.unitType, u.friendly)).foreach { img =>
                val iw = Math.max(1, (img.getWidth(null).toDouble  * scale).toInt)
                val ih = Math.max(1, (img.getHeight(null).toDouble * scale).toInt)
                val ix = (cx - iw / 2.0).toInt
                val iy = (cy - ih / 2.0).toInt
                g2.drawImage(img, ix, iy, iw, ih, null)
              }
              // Cooldown bar over image at bottom of circle bounds
              val cdMax = cdMaxById.getOrElse(u.id, Math.max(1, u.cd))
              if (u.cd >= 0 && cdMax > 0) {
                val fracCd = Math.max(0.0, Math.min(1.0, u.cd.toDouble / cdMax))
                val barH = Math.max(2, (2 * scale).toInt)
                val barY = (cy + r - barH).toInt
                val barW = Math.max(1, (diam * fracCd).toInt)
                g2.setColor(new Color(255, 215, 0, 220))
                g2.fillRect((cx - r).toInt, barY, barW, barH)
              }
            } else {
              val left = u.x - u.width / 2
              val top  = u.y - u.height / 2
              val sx = (offX + (left - baseX) * scale).toInt
              val sy = (offY + (top  - baseY) * scale).toInt
              val sw = Math.max(1, (u.width  * scale).toInt)
              val sh = Math.max(1, (u.height * scale).toInt)
              val filled = Math.max(1, (sh * frac).toInt)
              // Fill bottom portion
              g2.setColor(if (u.friendly) new Color(0, 0, 255, 140) else new Color(255, 0, 0, 140))
              g2.fillRect(sx, sy + (sh - filled), sw, filled)
              // Outline
              g2.setColor(color)
              g2.drawRect(sx, sy, sw, sh)
              // Draw unit image at intrinsic size scaled by global scale, centered on unit position
              metaById.get(u.id).flatMap(m => getRecoloredUnitImage(m.unitType, u.friendly)).foreach { img =>
                val cx = offX + (u.x - baseX) * scale
                val cy = offY + (u.y - baseY) * scale
                val iw = Math.max(1, (img.getWidth(null).toDouble  * scale).toInt)
                val ih = Math.max(1, (img.getHeight(null).toDouble * scale).toInt)
                val ix = (cx - iw / 2.0).toInt
                val iy = (cy - ih / 2.0).toInt
                g2.drawImage(img, ix, iy, iw, ih, null)
              }
              // Cooldown bar at bottom of box
              val cdMax = cdMaxById.getOrElse(u.id, Math.max(1, u.cd))
              if (u.cd >= 0 && cdMax > 0) {
                val fracCd = Math.max(0.0, Math.min(1.0, u.cd.toDouble / cdMax))
                val barH = Math.max(2, (2 * scale).toInt)
                val barY = sy + sh - barH
                val barW = Math.max(1, (sw * fracCd).toInt)
                g2.setColor(new Color(255, 215, 0, 220))
                g2.fillRect(sx, barY, barW, barH)
              }
            }
          }
        }
        // Draw death indicators (solid red) for deaths in the last 8 frames
        val recentIdxs = {
          val buf = new scala.collection.mutable.ArrayBuffer[Int](8)
          var k = 0
          while (k < 8) { val idx = frameIndex - k; if (idx >= 0 && idx < frames.length) buf += idx; k += 1 }
          buf.toVector
        }
        if (recentIdxs.nonEmpty) {
          g2.setColor(Color.RED)
          recentIdxs.foreach { idx =>
            val fdr = frames(idx)
            val pos = fdr.units.map(u => u.id -> u).toMap
            fdr.deaths.foreach { id =>
              pos.get(id).foreach { u =>
                if (u.fly) {
                  val cx = offX + (u.x - baseX) * scale
                  val cy = offY + (u.y - baseY) * scale
                  val diam = Math.max(u.width, u.height) * scale
                  val r = diam / 2.0
                  val ellipse = new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, diam, diam)
                  g2.fill(ellipse)
                } else {
                  val left = u.x - u.width / 2
                  val top  = u.y - u.height / 2
                  val sx = (offX + (left - baseX) * scale).toInt
                  val sy = (offY + (top  - baseY) * scale).toInt
                  val sw = Math.max(1, (u.width  * scale).toInt)
                  val sh = Math.max(1, (u.height * scale).toInt)
                  g2.fillRect(sx - 1, sy - 1, sw + 2, sh + 2)
                }
              }
            }
          }
        }
        // Flash victims white for the same recent window as attack lines, then draw attack lines on top
        if (recentIdxs.nonEmpty) {
          // Compute set of recent victims
          val victims = new java.util.HashSet[Int]()
          var vi = 0
          while (vi < recentIdxs.length) {
            val fdr = frames(recentIdxs(vi))
            var ak = 0
            while (ak < fdr.attacks.length) { victims.add(fdr.attacks(ak)._2); ak += 1 }
            vi += 1
          }
          if (!victims.isEmpty) {
            // Build a lookup for most recent positions to place the flash
            val latest = frames(recentIdxs.head).units.map(u => u.id -> u).toMap
            val it = victims.iterator()
            while (it.hasNext) {
              val id = it.next()
              latest.get(id).foreach { u =>
                // Flash the sprite image, not the HP background
                metas.find(_.id == u.id).flatMap(m => getRecoloredUnitImage(m.unitType, u.friendly)).foreach { img =>
                  val cx = offX + (u.x - baseX) * scale
                  val cy = offY + (u.y - baseY) * scale
                  val iw = Math.max(1, (img.getWidth(null).toDouble  * scale).toInt)
                  val ih = Math.max(1, (img.getHeight(null).toDouble * scale).toInt)
                  val ix = (cx - iw / 2.0).toInt
                  val iy = (cy - ih / 2.0).toInt
                  val oldComp = g2.getComposite
                  g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f))
                  g2.setColor(Color.WHITE)
                  g2.fillRect(ix, iy, iw, ih)
                  g2.setComposite(oldComp)
                }
              }
            }
          }
          // Draw attack lines in attacker bright color, persisted for 3 frames and on top with thicker stroke
          val oldStroke = g2.getStroke
          g2.setStroke(new BasicStroke(3f))
          recentIdxs.foreach { idx =>
            val fdr = frames(idx)
            val pos = fdr.units.map(u => u.id -> u).toMap
            fdr.attacks.foreach { case (a, v) =>
              (pos.get(a), pos.get(v)) match {
                case (Some(ua), Some(uv)) =>
                  val ax = (offX + (ua.x - baseX) * scale).toInt
                  val ay = (offY + (ua.y - baseY) * scale).toInt
                  val vx = (offX + (uv.x - baseX) * scale).toInt
                  val vy = (offY + (uv.y - baseY) * scale).toInt
                  val ac = if (ua.friendly) new Color(0, 0, 255, 220) else new Color(255, 0, 0, 220)
                  g2.setColor(ac)
                  g2.drawLine(ax, ay, vx, vy)
                case _ =>
              }
            }
          }
          g2.setStroke(oldStroke)
        }
      }
    }
  }

  // Sidebar panel showing a tally of unit deaths per side up to the current frame
  private class DeathTallyPanel(
    metasRef: java.util.concurrent.atomic.AtomicReference[Vector[UnitMeta]],
    framesRef: java.util.concurrent.atomic.AtomicReference[Vector[FrameData]],
    currentIndex: () => Int
  ) extends JPanel {
    setPreferredSize(new Dimension(140, 200))
    setOpaque(true)
    setBackground(new Color(24, 24, 24))
    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)
      val g2 = g.asInstanceOf[java.awt.Graphics2D]
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
      g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
      val blue = new Color(0, 0, 255)
      val red = new Color(255, 0, 0)
      val frames = framesRef.get()
      val idx = Math.max(0, Math.min(currentIndex(), Math.max(0, frames.length - 1)))
      val metas = metasRef.get().map(m => m.id -> m).toMap
      val deadF = new scala.collection.mutable.ArrayBuffer[Int]()
      val deadE = new scala.collection.mutable.ArrayBuffer[Int]()
      var i = 0
      while (i <= idx && i < frames.length) {
        val f = frames(i)
        var j = 0
        while (j < f.deaths.length) {
          val id = f.deaths(j)
          metas.get(id).foreach { m =>
            if (m.friendly) deadF += id else deadE += id
          }
          j += 1
        }
        i += 1
      }
      val uniqF = deadF.distinct
      val uniqE = deadE.distinct
      val w = getWidth
      val pad = 10
      val colW = (w - pad * 3) / 2
      val box = 10
      g2.setColor(Color.WHITE)
      g2.drawString("Deaths", pad, 14)
      // Totals just below header
      g2.drawString(s"F: ${uniqF.size}", pad, 26)
      g2.drawString(s"E: ${uniqE.size}", pad * 2 + colW, 26)
      // Columns from top with portraits
      val maxThumb = Math.max(12, Math.min(48, colW - 8))
      var y = 42
      // Friendly (left column)
      uniqF.foreach { id =>
        val xCol = pad
        metas.get(id).flatMap(m => getRecoloredUnitImage(m.unitType, friendly = true)) match {
          case Some(img) =>
            val iw = Math.max(1, img.getWidth(null))
            val ih = Math.max(1, img.getHeight(null))
            val scale = Math.min(maxThumb.toDouble / iw.toDouble, maxThumb.toDouble / ih.toDouble)
            val dw = Math.max(1, (iw * scale).toInt)
            val dh = Math.max(1, (ih * scale).toInt)
            val dx = xCol + (colW - dw) / 2
            val dy = y
            // Border
            g2.setColor(blue)
            g2.drawRect(dx - 1, dy - 1, dw + 2, dh + 2)
            // Image
            g2.drawImage(img, dx, dy, dw, dh, null)
            y += dh + 6
          case None =>
            g2.setColor(blue)
            g2.fillRect(xCol + (colW - box) / 2, y, box, box)
            y += box + 6
        }
      }
      // Enemy (right column)
      y = 28
      uniqE.foreach { id =>
        val xCol = pad * 2 + colW
        metas.get(id).flatMap(m => getRecoloredUnitImage(m.unitType, friendly = false)) match {
          case Some(img) =>
            val iw = Math.max(1, img.getWidth(null))
            val ih = Math.max(1, img.getHeight(null))
            val scale = Math.min(maxThumb.toDouble / iw.toDouble, maxThumb.toDouble / ih.toDouble)
            val dw = Math.max(1, (iw * scale).toInt)
            val dh = Math.max(1, (ih * scale).toInt)
            val dx = xCol + (colW - dw) / 2
            val dy = y
            g2.setColor(red)
            g2.drawRect(dx - 1, dy - 1, dw + 2, dh + 2)
            g2.drawImage(img, dx, dy, dw, dh, null)
            y += dh + 6
          case None =>
            g2.setColor(red)
            g2.fillRect(xCol + (colW - box) / 2, y, box, box)
            y += box + 6
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    // Determine simulation file path (.pwcs preferred, fallback to .json)
    // Sanitize Launch4j-injected literal "%1" and pick a usable path argument if provided
    val cleanedArgs: Vector[String] =
      Option(args).map(_.toVector).getOrElse(Vector.empty)
        .filter(s => s != null && s.trim.nonEmpty)
        .filterNot(_ == "%1")
    // Prefer the first argument that points to an existing file; otherwise use the first remaining
    val chosenArgOpt: Option[String] =
      cleanedArgs.find(p => try new File(p).exists() catch { case _: Throwable => false }).orElse(cleanedArgs.headOption)
    val argPathOpt = chosenArgOpt
    val simFile: File = {
      val candidate = argPathOpt.map(new File(_)).getOrElse(new File("simulation.pwcs"))
      val f =
        if (candidate.isDirectory) {
          val pwcs = new File(candidate, "simulation.pwcs")
          if (pwcs.exists()) pwcs else new File(candidate, "simulation.json")
        } else if (candidate.exists()) candidate
        else {
          val pwcs = new File("simulation.pwcs")
          if (pwcs.exists()) pwcs else new File("simulation.json")
        }
      f
    }
    val argStr = Option(args).map(_.mkString(" ")).getOrElse("<none>")
    err("Startup args: " + argStr)
    err("Resolved sim file: " + simFile.getAbsolutePath + s" exists=${simFile.exists()} len=${if (simFile.exists()) simFile.length() else -1} lm=${simFile.lastModified()}")
    try { extVisLog(s"Startup args='$argStr' file='${simFile.getAbsolutePath}' exists=${simFile.exists()} len=${if (simFile.exists()) simFile.length() else -1}") } catch { case _: Throwable => }
    val baseDir = simFile.getParentFile
    currentSimDir = baseDir
    val mapDir = baseDir

    // State shared with UI thread
    val lastModifiedRef = new java.util.concurrent.atomic.AtomicLong(0L)
    val autoReloadRef = new java.util.concurrent.atomic.AtomicBoolean(true)
    val mapRef = new java.util.concurrent.atomic.AtomicReference[Option[MapData]](None)
    val framesRef = new java.util.concurrent.atomic.AtomicReference[Vector[FrameData]](Vector.empty)
    val metasRef = new java.util.concurrent.atomic.AtomicReference[Vector[UnitMeta]](Vector.empty)
    val simsRef = new java.util.concurrent.atomic.AtomicReference[Vector[SimPack]](Vector.empty)
    // Incremental reading state
    val fileLenRef = new java.util.concurrent.atomic.AtomicLong(0L)
    val pendingRef = new java.util.concurrent.atomic.AtomicReference[String]("")

    def readAllText(f: File): String = {
      var fis: java.io.FileInputStream = null
      var bis: java.io.BufferedInputStream = null
      try {
        fis = new FileInputStream(f)
        bis = new java.io.BufferedInputStream(fis)
        bis.mark(4)
        val b1 = bis.read(); val b2 = bis.read(); bis.reset()
        val isGz = (b1 == 0x1f && b2 == 0x8b)
        val in: java.io.InputStream = if (isGz) new GZIPInputStream(bis) else bis
        try scala.io.Source.fromInputStream(in)(scala.io.Codec.UTF8).mkString finally in.close()
      } finally {
        try if (bis != null) bis.close() catch { case _: Throwable => }
        try if (fis != null) fis.close() catch { case _: Throwable => }
      }
    }

    def splitPW2Blocks(txt: String): Vector[String] = {
      val lines = txt.split("\r?\n")
      val out = new scala.collection.mutable.ArrayBuffer[String]()
      var i = 0
      var cur = new StringBuilder()
      var in = false
      while (i < lines.length) {
        val line = lines(i)
        val lt = if (line == null) "" else line.trim
        if (lt == "PW2") {
          if (in) { cur = new StringBuilder() }
          in = true
          cur.append("PW2\n")
        } else if (in) {
          cur.append(line).append('\n')
          if (lt == "Z") { out += cur.toString(); cur = new StringBuilder(); in = false }
        }
        i += 1
      }
      out.toVector
    }

    // Streaming splitter: returns (completeBlocks, remainder)
    def splitPW2BlocksWithRemainder(txt: String): (Vector[String], String) = {
      val lines = txt.split("\r?\n", -1) // keep trailing empty
      val out = new scala.collection.mutable.ArrayBuffer[String]()
      val cur = new StringBuilder()
      var in = false
      var i = 0
      while (i < lines.length) {
        val line = lines(i)
        val lt = if (line == null) "" else line.trim
        if (lt == "PW2") {
          if (in) {
            // Unexpected new PW2 without Z; discard previous partial and start fresh
            cur.clear()
          }
          in = true
          cur.append("PW2\n")
        } else if (in) {
          cur.append(line).append('\n')
          if (lt == "Z") {
            out += cur.toString()
            cur.clear()
            in = false
          }
        }
        i += 1
      }
      val rem = if (in && cur.length > 0) cur.toString() else ""
      (out.toVector, rem)
    }

    def readAppended(file: File): (String, Long, Boolean) = {
      var raf: RandomAccessFile = null
      try {
        raf = new RandomAccessFile(file, "r")
        val curLen = fileLenRef.get()
        val fileLen = raf.length()
        if (fileLen < curLen) {
          // Truncated (new game) – reset
          fileLenRef.set(0L)
          raf.seek(0L)
          val bytes = new Array[Byte](fileLen.toInt)
          raf.readFully(bytes)
          (new String(bytes, java.nio.charset.StandardCharsets.UTF_8), fileLen, true)
        } else if (fileLen > curLen) {
          raf.seek(curLen)
          val toRead = (fileLen - curLen).toInt
          val bytes = new Array[Byte](toRead)
          raf.readFully(bytes)
          (new String(bytes, java.nio.charset.StandardCharsets.UTF_8), fileLen, false)
        } else {
          ("", fileLen, false)
        }
      } catch { case _: Throwable => ("", fileLenRef.get(), false) }
      finally { try if (raf != null) raf.close() catch { case _: Throwable => } }
    }

    def parseAllSims(file: File): Vector[SimPack] = {
      if (!file.exists()) { err("parseAllSims: file does not exist: " + file.getAbsolutePath); return Vector.empty }
      // Stream the file and keep only the last N complete PW2 blocks to bound memory
      val MaxSimsToKeep = 200
      var fis: java.io.FileInputStream = null
      var bis: java.io.BufferedInputStream = null
      try {
        fis = new FileInputStream(file)
        bis = new java.io.BufferedInputStream(fis, 1 << 16)
        val buf = new Array[Byte](1 << 15) // 32 KB chunks
        val deque = new java.util.ArrayDeque[String]()
        var pending = ""
        var read = 0
        var totalRead = 0L
        while ({ read = bis.read(buf); read } != -1) {
          totalRead += read
          val seg = new String(buf, 0, read, java.nio.charset.StandardCharsets.UTF_8)
          val (blocks, rem) = splitPW2BlocksWithRemainder(pending + seg)
          pending = rem
          var i = 0
          while (i < blocks.length) {
            if (deque.size() >= MaxSimsToKeep) deque.pollFirst()
            deque.addLast(blocks(i))
            i += 1
          }
        }
        // If we saw no PW2 blocks at all, try legacy JSON by reading the entire (likely small) file
        if (deque.isEmpty) {
          err(s"parseAllSims: no PW2 blocks found in streaming read (bytes=$totalRead); trying full read")
          var txt = readAllText(file)
          if (txt != null && txt.nonEmpty && txt.charAt(0) == '\uFEFF') {
            // Strip UTF-8 BOM if present
            txt = txt.substring(1)
          }
          val head = if (txt.length > 64) txt.substring(0, 64).replace('\n', ' ') else txt
          err("parseAllSims: full-read head='" + head + "'")
          val parseFromText: Vector[SimPack] = {
            if (txt.startsWith("PW2")) {
              val blocks = splitPW2Blocks(txt)
              err("parseAllSims: PW2 full-read blocks=" + blocks.length)
              blocks.flatMap { b => Try(parsePW2(b)).toOption }.sortBy(s => -s.startGameFrame)
            } else {
              // Try regex-based extraction tolerant to extra whitespace lines and mixed endings
              try {
                val pattern = java.util.regex.Pattern.compile("(?ms)^\\s*PW2\\s*$.*?^\\s*Z\\s*$")
                val m = pattern.matcher(txt)
                val buf = new scala.collection.mutable.ArrayBuffer[String]()
                while (m.find()) { buf += m.group() }
                if (buf.nonEmpty) {
                  err("parseAllSims: regex PW2 blocks found=" + buf.length)
                  buf.flatMap(b => Try(parsePW2(b)).toOption).toVector.sortBy(s => -s.startGameFrame)
                } else {
                  err("parseAllSims: non-PW2; attempting legacy JSON parse")
                  Vector(parseJsonSim(txt))
                }
              } catch { case _: Throwable => Vector(parseJsonSim(txt)) }
            }
          }
          err("parseAllSims: fallback sims parsed=" + parseFromText.length)
          parseFromText
        } else {
          // Parse the kept blocks to SimPacks and sort newest first
          err("parseAllSims: streaming PW2 blocks found=" + deque.size())
          val sims = new scala.collection.mutable.ArrayBuffer[SimPack](deque.size())
          val it = deque.iterator()
          while (it.hasNext) {
            val blk = it.next()
            Try(parsePW2(blk)).foreach(sims += _)
          }
          val out = sims.toVector.sortBy(s => -s.startGameFrame)
          err("parseAllSims: sims parsed=" + out.length)
          out
        }
      } catch { case t: Throwable => err("parseAllSims exception: " + t.toString); Vector.empty }
      finally {
        try if (bis != null) bis.close() catch { case _: Throwable => }
        try if (fis != null) fis.close() catch { case _: Throwable => }
      }
    }

    // cooldown maxima per unit id
    def computeCdMax(frames: Vector[FrameData]): Map[Int, Int] = {
      val m = new scala.collection.mutable.HashMap[Int, Int]()
      var i = 0
      while (i < frames.length) {
        val f = frames(i)
        var j = 0
        while (j < f.units.length) {
          val u = f.units(j)
          if (u.cd >= 0) m.put(u.id, Math.max(m.getOrElse(u.id, 0), u.cd))
          j += 1
        }
        i += 1
      }
      m.toMap
    }
    val cdMaxRef = new java.util.concurrent.atomic.AtomicReference[Map[Int, Int]](Map.empty)

    // Auto-zoom bounds
    val zoomRef = new java.util.concurrent.atomic.AtomicReference[Option[(Int, Int, Int, Int)]](None)

    // Initial load
    def computeWorldSize(frames: Vector[FrameData]): (Int, Int) = {
      var maxX = 0
      var maxY = 0
      var i = 0
      while (i < frames.length) {
        val f = frames(i)
        var j = 0
        while (j < f.units.length) {
          val u = f.units(j)
          if (u.x > maxX) maxX = u.x
          if (u.y > maxY) maxY = u.y
          j += 1
        }
        i += 1
      }
      (Math.max(1024, maxX + 64), Math.max(768, maxY + 64))
    }
    def computeAutoZoomBounds(frames: Vector[FrameData]): Option[(Int, Int, Int, Int)] = {
      if (frames.isEmpty) return None
      // Collect IDs that deal or receive damage (attackers, victims) and deaths
      val ids = new scala.collection.mutable.HashSet[Int]()
      var i = 0
      while (i < frames.length) {
        val f = frames(i)
        var k = 0
        while (k < f.attacks.length) {
          val pair = f.attacks(k)
          ids += pair._1; ids += pair._2
          k += 1
        }
        var d = 0
        while (d < f.deaths.length) { ids += f.deaths(d); d += 1 }
        i += 1
      }
      if (ids.isEmpty) return None
      var minX = Int.MaxValue
      var minY = Int.MaxValue
      var maxX = Int.MinValue
      var maxY = Int.MinValue
      i = 0
      while (i < frames.length) {
        val f = frames(i)
        var j = 0
        while (j < f.units.length) {
          val u = f.units(j)
          if (ids.contains(u.id)) {
            if (u.x < minX) minX = u.x
            if (u.y < minY) minY = u.y
            if (u.x > maxX) maxX = u.x
            if (u.y > maxY) maxY = u.y
          }
          j += 1
        }
        i += 1
      }
      if (minX == Int.MaxValue) None
      else {
        val margin = 64
        // Best-fit rectangle including margin (no squaring)
        val bx = Math.max(0, minX - margin)
        val by = Math.max(0, minY - margin)
        val bw = Math.max(1, (maxX - minX) + margin * 2)
        val bh = Math.max(1, (maxY - minY) + margin * 2)
        Some((bx, by, bw, bh))
      }
    }
    // Initial parse of all sims and load newest
    var all0 = parseAllSims(simFile)
    // Fallback: if nothing parsed (e.g., gzipped or legacy), try a full read
    if (all0.isEmpty && simFile.exists()) {
      try {
        val txt = readAllText(simFile)
        if (txt != null && txt.nonEmpty) {
          if (txt.startsWith("PW2")) {
            val blocks = splitPW2Blocks(txt)
            all0 = blocks.flatMap(b => Try(parsePW2(b)).toOption).sortBy(s => -s.startGameFrame)
          } else {
            all0 = Vector(parseJsonSim(txt))
          }
        }
      } catch { case _: Throwable => () }
    }
    simsRef.set(all0)
    if (all0.nonEmpty) {
      val pack = all0.head
      mapRef.set(loadMap(mapDir, pack.mapHash))
      framesRef.set(pack.frames)
      metasRef.set(pack.metas)
      cdMaxRef.set(computeCdMax(pack.frames))
      zoomRef.set(computeAutoZoomBounds(pack.frames))
    } else {
      try { extVisLog(s"Initial load found no simulations in ${simFile.getAbsolutePath}") } catch { case _: Throwable => }
    }
    lastModifiedRef.set(simFile.lastModified())
    fileLenRef.set(if (simFile.exists()) simFile.length() else 0L)
    pendingRef.set("")

    SwingUtilities.invokeLater(new Runnable { override def run(): Unit = {
      val panel = new ViewPanel(mapRef.get(), framesRef.get())
      panel.metas = metasRef.get()
      panel.cdMaxById = cdMaxRef.get()
      // Initialize fallback world size if map missing
      if (panel.map.isEmpty) {
        val (ww, wh) = computeWorldSize(framesRef.get())
        panel.worldW = ww
        panel.worldH = wh
      }
      // Initialize auto-zoom
      panel.zoomRect = zoomRef.get()
      panel.autoZoomEnabled = true

      // Controls
      val playPause = new JButton("Pause")
      val reloadBtn = new JButton("Reload")
      val autoReload = new JCheckBox("Auto-reload", true)
      val autoZoom = new JCheckBox("Auto Zoom", true)
      val timeLabel = new JLabel("")
      val slider = new JSlider()
      slider.setMinimum(0)
      slider.setMaximum(Math.max(0, Math.max(0, panel.frames.length - 1)))
      slider.setValue(0)
      slider.setPaintTicks(false)
      slider.setPaintLabels(false)

      def updateTimeLabel(): Unit = {
        if (panel.frames.nonEmpty) {
          val minF = panel.frames.head.f
          val maxF = panel.frames.last.f
          val cur  = panel.currentFrameData.map(_.f).getOrElse(0)
          timeLabel.setText(f"Frame $cur%,d  (range $minF%,d .. $maxF%,d)")
        } else timeLabel.setText("No frames")
      }

      // Frame
      val frame = new JFrame("PurpleSimViz")
      window = frame
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
      frame.setLayout(new BorderLayout())

      // Left simulation list
      val simListModel = new DefaultListModel[String]()
      val simList = new JList[String](simListModel)
      simList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
      val simScroll = new JScrollPane(simList)
      simScroll.setPreferredSize(new Dimension(100, 200))
      frame.getContentPane.add(simScroll, BorderLayout.WEST)

      def labelFor(pack: SimPack): String = {
        val totalSec = Math.max(0, pack.startGameFrame / 24)
        val mm = totalSec / 60
        val ss = totalSec % 60
        val fmod = Math.floorMod(pack.startGameFrame, 24)
        f"$mm%d:$ss%02d +$fmod%d"
      }
      def populateSimList(sims: Vector[SimPack]): Unit = {
        simListModel.clear()
        // Show all simulations, including empty placeholders, so the user always has a selectable tab
        var i = 0
        val labels = new scala.collection.mutable.ArrayBuffer[String]()
        while (i < sims.length) { val lab = labelFor(sims(i)); simListModel.addElement(lab); labels += lab; i += 1 }
        try { extVisLog(s"populateSimList: count=${sims.length} labels=${labels.mkString(", ")}") } catch { case _: Throwable => }
      }
      def loadSimAt(index: Int): Unit = {
        val sims = simsRef.get()
        if (index >= 0 && index < sims.length) {
          val pack = sims(index)
          mapRef.set(loadMap(mapDir, pack.mapHash))
          framesRef.set(pack.frames)
          metasRef.set(pack.metas)
          cdMaxRef.set(computeCdMax(pack.frames))
          zoomRef.set(computeAutoZoomBounds(pack.frames))
          panel.map = mapRef.get()
          panel.frames = pack.frames
          panel.metas = pack.metas
          panel.cdMaxById = cdMaxRef.get()
          panel.zoomRect = zoomRef.get()
          if (panel.map.isEmpty) {
            val (ww, wh) = computeWorldSize(pack.frames)
            panel.worldW = ww; panel.worldH = wh
          }
          slider.setMaximum(Math.max(0, pack.frames.length - 1))
          panel.frameIndex = Math.min(panel.frameIndex, Math.max(0, pack.frames.length - 1))
          slider.setValue(panel.frameIndex)
          panel.revalidate(); panel.repaint(); updateTimeLabel()
        }
      }

      // Death tally panel on the right
      val tally = new DeathTallyPanel(metasRef, framesRef, () => panel.frameIndex)
      frame.getContentPane.add(panel, BorderLayout.CENTER)
      frame.getContentPane.add(tally, BorderLayout.EAST)

      // Populate list initially
      populateSimList(simsRef.get())
      if (simsRef.get().nonEmpty) {
        val simsNow = simsRef.get()
        var idx = 0
        var found = false
        while (idx < simsNow.length && !found) {
          if (simsNow(idx).frames.nonEmpty) found = true else idx += 1
        }
        val sel = if (found) idx else 0
        simList.setSelectedIndex(sel)
        loadSimAt(sel)
      }

      // Disable auto-reload on manual selection and load selected sim
      simList.addListSelectionListener(_ => {
        if (!simList.getValueIsAdjusting) {
          val idx = simList.getSelectedIndex
          if (idx >= 0) {
            autoReloadRef.set(false)
            autoReload.setSelected(false)
            loadSimAt(idx)
          }
        }
      })

      val controls = new JPanel()
      controls.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT))
      controls.add(playPause)
      controls.add(slider)
      // Playback speed slider (1x,2x,3x,4x,6x,8x)
      val speedOptions = Array(1, 2, 3, 4, 6, 8)
      val speedSlider = new JSlider(0, speedOptions.length - 1, 4)
      val speedLabels = new java.util.Hashtable[Integer, JComponent]()
      var si = 0
      while (si < speedOptions.length) { speedLabels.put(Integer.valueOf(si), new JLabel(speedOptions(si) + "x")); si += 1 }
      speedSlider.setLabelTable(speedLabels)
      speedSlider.setPaintLabels(true)
      speedSlider.setPaintTicks(true)
      speedSlider.setSnapToTicks(true)
      controls.add(new JLabel("Speed:"))
      controls.add(speedSlider)
      controls.add(new JLabel(" "))
      controls.add(autoZoom)
      controls.add(timeLabel)
      controls.add(autoReload)
      controls.add(reloadBtn)
      frame.getContentPane.add(controls, BorderLayout.SOUTH)

      frame.setSize(new Dimension(1200, 800))
      frame.setLocationByPlatform(true)
      frame.setVisible(true)

      // Timer ~24 FPS
      val playingRef = new java.util.concurrent.atomic.AtomicBoolean(true)

      // Async reload machinery
      val reloadInProgress = new java.util.concurrent.atomic.AtomicBoolean(false)
      val bgExecutor = java.util.concurrent.Executors.newSingleThreadExecutor(new java.util.concurrent.ThreadFactory {
        override def newThread(r: Runnable): Thread = {
          val t = new Thread(r, "CombatVisualizer-Reload")
          t.setDaemon(true)
          t
        }
      })
      def reloadIfChanged(force: Boolean = false): Unit = {
        val lm = simFile.lastModified()
        val lenNow = try simFile.length() catch { case _: Throwable => 0L }
        if (!(force || (autoReloadRef.get() && (lm > lastModifiedRef.get() || lenNow > fileLenRef.get())))) return
        if (!reloadInProgress.compareAndSet(false, true)) return
        bgExecutor.submit(new Runnable { override def run(): Unit = {
          val t0 = System.nanoTime()
          try {
            // Incremental read of appended bytes
            val (seg, newLen, truncated) = readAppended(simFile)
            var newBlocks = Vector.empty[String]
            if (truncated) {
              // File was truncated (new game). Reparse recent sims using streaming bounded loader to avoid OOM.
              pendingRef.set("")
              val sims = parseAllSims(simFile)
              javax.swing.SwingUtilities.invokeLater(new Runnable { override def run(): Unit = {
                try {
                  simsRef.set(sims)
                  populateSimList(sims)
                  val nonEmptySims = sims.filter(p => p.frames.nonEmpty)
                  val idxToLoad = if (nonEmptySims.nonEmpty) 0 else -1
                  if (idxToLoad >= 0) loadSimAt(idxToLoad)
                } catch { case _: Throwable => () }
              }})
              // Update length and modified time, then return (no merging needed)
              lastModifiedRef.set(lm)
              fileLenRef.set(newLen)
              val ms = (System.nanoTime() - t0) / 1000000L
              try { System.out.println(s"[CV] reload truncated; rebuilt list with ${sims.length} sims in ${ms}ms") } catch { case _: Throwable => () }
              return
            } else if (seg.nonEmpty) {
              val combined = pendingRef.get() + seg
              val (blocks, rem) = splitPW2BlocksWithRemainder(combined)
              newBlocks = blocks
              pendingRef.set(rem)
            }
            if (newBlocks.nonEmpty) {
              val parsed = newBlocks.flatMap(b => Try(parsePW2(b)).toOption)
              if (parsed.nonEmpty) {
                // Merge into existing, de-duplicate, bound to last 200, sort newest first
                def keyFor(p: SimPack): String = {
                  val ids = p.metas.map(_.id).sorted.mkString(",")
                  p.startGameFrame.toString + "|" + ids
                }
                val before = simsRef.get()
                val mergedAll = parsed ++ before
                val dedupMap = new scala.collection.mutable.LinkedHashMap[String, SimPack]()
                var mi = 0
                while (mi < mergedAll.length) {
                  val sp = mergedAll(mi)
                  val k = keyFor(sp)
                  if (!dedupMap.contains(k)) dedupMap.put(k, sp)
                  mi += 1
                }
                val dedup = dedupMap.values.toVector.sortBy(s => -s.startGameFrame)
                val bounded = if (dedup.length > 200) dedup.take(200) else dedup
                javax.swing.SwingUtilities.invokeLater(new Runnable { override def run(): Unit = {
                  try {
                    simsRef.set(bounded)
                    populateSimList(bounded)
                    val hasManualSelection = !autoReloadRef.get()
                    val sel = simList.getSelectedIndex
                    val idxToLoad = if (hasManualSelection && sel >= 0 && sel < bounded.length) sel else 0
                    loadSimAt(idxToLoad)
                    try { extVisLog(s"merge parsed=${parsed.length} before=${before.length} after=${bounded.length}") } catch { case _: Throwable => }
                  } catch { case _: Throwable => () }
                }})
              }
            }
            lastModifiedRef.set(lm)
            fileLenRef.set(newLen)
            val ms = (System.nanoTime() - t0) / 1000000L
            try { System.out.println(s"[CV] reload parsed ${newBlocks.size} new blocks in ${ms}ms; sims=${simsRef.get().length}") } catch { case _: Throwable => () }
          } catch { case _: Throwable => () }
          finally { reloadInProgress.set(false) }
        }})
      }

      val timer = new javax.swing.Timer(1000 / 24, (_: java.awt.event.ActionEvent) => {
        try {
          // If no frames, attempt auto-reload opportunistically
          if (panel.frames.isEmpty) {
            if (autoReloadRef.get()) reloadIfChanged(force = false)
          }
          if (panel.frames.nonEmpty && playingRef.get()) {
            val lastIdx = panel.frames.length - 1
            val speed = {
              val idx = speedSlider.getValue
              val clamped = Math.max(0, Math.min(idx, speedOptions.length - 1))
              speedOptions(clamped)
            }
            if (panel.frameIndex >= lastIdx) {
              // At end of playback: attempt auto-reload if enabled
              if (autoReloadRef.get()) reloadIfChanged(force = false)
              panel.frameIndex = 0
            } else {
              panel.frameIndex = Math.min(lastIdx, panel.frameIndex + speed)
            }
            slider.setValue(panel.frameIndex)
          }
          updateTimeLabel()
          panel.repaint()
          tally.repaint()
        } catch { case _: Throwable => () }
      })
      timerRef = timer
      frame.addWindowListener(new java.awt.event.WindowAdapter() {
        override def windowClosed(e: java.awt.event.WindowEvent): Unit = {
          try { if (timerRef != null) timerRef.stop() } catch { case _: Throwable => }
          timerRef = null
          window = null
        }
        override def windowClosing(e: java.awt.event.WindowEvent): Unit = {
          try { if (timerRef != null) timerRef.stop() } catch { case _: Throwable => }
        }
      })

      // Wire controls
      playPause.addActionListener(_ => {
        val now = !playingRef.get()
        playingRef.set(now)
        playPause.setText(if (now) "Pause" else "Play")
      })

      autoReload.addActionListener(_ => {
        autoReloadRef.set(autoReload.isSelected)
      })

      autoZoom.addActionListener(_ => {
        panel.autoZoomEnabled = autoZoom.isSelected
        panel.repaint()
      })

      reloadBtn.addActionListener(_ => reloadIfChanged(force = true))

      slider.addChangeListener(_ => {
        val idx = slider.getValue
        panel.frameIndex = Math.max(0, Math.min(idx, Math.max(0, panel.frames.length - 1)))
        updateTimeLabel()
        panel.repaint()
      })

      // Start
      updateTimeLabel()
      timer.start()
    }})
  }
}
