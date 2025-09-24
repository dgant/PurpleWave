package Debugging

import mjson.Json

import java.awt.image.BufferedImage
import java.awt._
import java.io.{DataInputStream, File, FileInputStream}
import javax.swing._
import scala.util.Try

/**
  * Combat simulation viewer with scaling, playback controls, and auto-reload toggle.
  * Usage: Debugging.CombatVisualizer <path-to-simulation.pwcs>
  * If no path is provided, defaults to simulation.pwcs in the working directory (falls back to simulation.json).
  */
object CombatVisualizer {

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

  case class UnitMeta(id: Int, friendly: Boolean, width: Int, height: Int, unitType: String)
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

  private def parseSimulation(jsonFile: File): Option[(String, Vector[UnitMeta], Vector[FrameData])] = {
    if (!jsonFile.exists()) return None
    Try {
      val txt = scala.io.Source.fromFile(jsonFile)(scala.io.Codec.UTF8).mkString
      val j = Json.read(txt)
      val mapHash = j.at("mapHash").asString()
      val unitsJ = Option(j.at("units")).map(_.asJsonList()).map(_.toArray(new Array[Json](0)).toVector).getOrElse(Vector.empty)
      val metas = unitsJ.flatMap { u =>
        Try(UnitMeta(
          id = u.at("id").asInteger(),
          friendly = u.at("friendly").asBoolean(),
          width = u.at("width").asInteger(),
          height = u.at("height").asInteger(),
          unitType = Try(u.at("type").asString()).getOrElse("")
        )).toOption
      }
      val framesJ = Option(j.at("frames")).map(_.asJsonList()).map(_.toArray(new Array[Json](0)).toVector).getOrElse(Vector.empty)
      val frames = framesJ.flatMap { fj =>
        val fnum = Try(fj.at("f").asInteger()).getOrElse(0)
        val unitsArr = Option(fj.at("u")).map(_.asJsonList()).map(_.toArray(new Array[Json](0)).toVector).getOrElse(Vector.empty)
        val units = unitsArr.flatMap { uj =>
          val base = Try((
            uj.at("id").asInteger(),
            uj.at("friendly").asBoolean(),
            uj.at("x").asInteger(),
            uj.at("y").asInteger(),
            uj.at("alive").asBoolean(),
            uj.at("width").asInteger(),
            uj.at("height").asInteger()
          )).toOption
          base.map { case (id, fr, x, y, alive, w, h) =>
            val hp    = Try(uj.at("hp").asInteger()).getOrElse(-1)
            val sh    = Try(uj.at("sh").asInteger()).getOrElse(-1)
            val hpMax = Try(uj.at("hpMax").asInteger()).getOrElse(-1)
            val shMax = Try(uj.at("shMax").asInteger()).getOrElse(-1)
            val tgt   = Try(uj.at("tgt").asInteger()).toOption
            val fly   = Try(uj.at("fly").asBoolean()).getOrElse(false)
            val cd    = Try(uj.at("cd").asInteger()).getOrElse(-1)
            UnitFrame(id, fr, x, y, alive, w, h, hp, sh, hpMax, shMax, tgt, fly, cd)
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
      (mapHash, metas, frames)
    }.toOption
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
      val url = CombatVisualizer.getClass.getProtectionDomain.getCodeSource.getLocation
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
        // Draw attack lines in attacker bright color, persisted for 3 frames and on top with thicker stroke
        if (recentIdxs.nonEmpty) {
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
      // Columns from top with portraits
      val maxThumb = Math.max(12, Math.min(48, colW - 8))
      var y = 28
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
      g2.setColor(Color.WHITE)
      g2.drawString(s"F: ${uniqF.size}", pad, getHeight - 10)
      g2.drawString(s"E: ${uniqE.size}", pad * 2 + colW, getHeight - 10)
    }
  }

  def main(args: Array[String]): Unit = {
    // Determine simulation file path (.pwcs preferred, fallback to .json)
    val argPathOpt = if (args != null && args.length > 0) Option(args(0)) else None
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
    val baseDir = simFile.getParentFile
    currentSimDir = baseDir
    val mapDir = new File(baseDir, "maps")

    // State shared with UI thread
    val lastModifiedRef = new java.util.concurrent.atomic.AtomicLong(0L)
    val autoReloadRef = new java.util.concurrent.atomic.AtomicBoolean(true)
    val mapRef = new java.util.concurrent.atomic.AtomicReference[Option[MapData]](None)
    val framesRef = new java.util.concurrent.atomic.AtomicReference[Vector[FrameData]](Vector.empty)

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
    val metasRef = new java.util.concurrent.atomic.AtomicReference[Vector[UnitMeta]](Vector.empty)
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
    parseSimulation(simFile).foreach { case (hash, metas, frames) =>
      mapRef.set(loadMap(mapDir, hash))
      framesRef.set(frames)
      metasRef.set(metas)
      cdMaxRef.set(computeCdMax(frames))
      zoomRef.set(computeAutoZoomBounds(frames))
      lastModifiedRef.set(simFile.lastModified())
    }

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
      val frame = new JFrame("PurpleWave Combat Visualizer")
      window = frame
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
      frame.setLayout(new BorderLayout())
      frame.getContentPane.add(panel, BorderLayout.CENTER)

      // Death tally panel on the right
      val tally = new DeathTallyPanel(metasRef, framesRef, () => panel.frameIndex)
      frame.getContentPane.add(tally, BorderLayout.EAST)

      val controls = new JPanel()
      controls.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT))
      controls.add(playPause)
      controls.add(slider)
      // Playback speed slider (1x,2x,3x,4x,6x,8x)
      val speedOptions = Array(1, 2, 3, 4, 6, 8)
      val speedSlider = new JSlider(0, speedOptions.length - 1, 0)
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

      def reloadIfChanged(force: Boolean = false): Unit = {
        val lm = simFile.lastModified()
        if (force || (autoReloadRef.get() && lm > lastModifiedRef.get())) {
          parseSimulation(simFile).foreach { case (hash, metas, frames) =>
            mapRef.set(loadMap(mapDir, hash))
            framesRef.set(frames)
            metasRef.set(metas)
            cdMaxRef.set(computeCdMax(frames))
            zoomRef.set(computeAutoZoomBounds(frames))
            panel.map = mapRef.get()
            panel.frames = frames
            panel.metas = metas
            panel.cdMaxById = cdMaxRef.get()
            panel.zoomRect = zoomRef.get()
            // Update fallback world size if map missing
            if (panel.map.isEmpty) {
              val (ww, wh) = computeWorldSize(frames)
              panel.worldW = ww
              panel.worldH = wh
            }
            // Update slider range and keep current index within bounds
            slider.setMaximum(Math.max(0, frames.length - 1))
            panel.frameIndex = Math.min(panel.frameIndex, Math.max(0, frames.length - 1))
            slider.setValue(panel.frameIndex)
            panel.revalidate()
            panel.repaint()
            tally.repaint()
            updateTimeLabel()
          }
          lastModifiedRef.set(lm)
        }
      }

      val timer = new javax.swing.Timer(1000 / 24, (_: java.awt.event.ActionEvent) => {
        // If no frames, attempt auto-reload opportunistically
        if (panel.frames.isEmpty) {
          if (autoReloadRef.get()) reloadIfChanged(force = false)
        }
        if (panel.frames.nonEmpty && playingRef.get()) {
          val lastIdx = panel.frames.length - 1
          val speed = speedOptions(speedSlider.getValue)
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
