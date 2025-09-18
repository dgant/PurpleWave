package Debugging

import mjson.Json

import java.awt.image.BufferedImage
import java.awt.{BorderLayout, Color, Dimension, Graphics, RenderingHints}
import java.io.{DataInputStream, File, FileInputStream}
import javax.swing._
import scala.util.Try

/**
  * Combat simulation viewer with scaling, playback controls, and auto-reload toggle.
  * Usage: Debugging.CombatVisualizer <path-to-simulation.json>
  */
object CombatVisualizer {

  @volatile private var window: JFrame = _
  @volatile private var timerRef: javax.swing.Timer = _
  def requestClose(): Unit = {
    try {
      SwingUtilities.invokeLater(new Runnable { override def run(): Unit = {
        try { if (timerRef != null) timerRef.stop() } catch { case _: Throwable => }
        try { if (window != null) window.dispose() } catch { case _: Throwable => }
      }})
    } catch { case _: Throwable => }
  }

  case class UnitMeta(id: Int, friendly: Boolean, width: Int, height: Int)
  case class UnitFrame(id: Int, friendly: Boolean, x: Int, y: Int, alive: Boolean, width: Int, height: Int)
  case class FrameData(f: Int, units: Vector[UnitFrame])

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
          height = u.at("height").asInteger()
        )).toOption
      }
      val framesJ = Option(j.at("frames")).map(_.asJsonList()).map(_.toArray(new Array[Json](0)).toVector).getOrElse(Vector.empty)
      val frames = framesJ.flatMap { fj =>
        val fnum = Try(fj.at("f").asInteger()).getOrElse(0)
        val unitsArr = Option(fj.at("u")).map(_.asJsonList()).map(_.toArray(new Array[Json](0)).toVector).getOrElse(Vector.empty)
        val units = unitsArr.flatMap { uj =>
          Try(UnitFrame(
            id = uj.at("id").asInteger(),
            friendly = uj.at("friendly").asBoolean(),
            x = uj.at("x").asInteger(),
            y = uj.at("y").asInteger(),
            alive = uj.at("alive").asBoolean(),
            width = uj.at("width").asInteger(),
            height = uj.at("height").asInteger()
          )).toOption
        }
        Some(FrameData(fnum, units))
      }
      (mapHash, metas, frames)
    }.toOption
  }

  private class ViewPanel(var map: Option[MapData], var frames: Vector[FrameData]) extends JPanel {
    setPreferredSize(new Dimension(map.map(_.pixelW).getOrElse(800), map.map(_.pixelH).getOrElse(600)))
    setDoubleBuffered(true)

    // Playback state
    @volatile var frameIndex = 0

    def currentFrameData: Option[FrameData] = if (frames.isEmpty) None else Some(frames(Math.max(0, Math.min(frameIndex, frames.length - 1))))

    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)
      val g2 = g.asInstanceOf[java.awt.Graphics2D]
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

      val w = getWidth
      val h = getHeight

      // Compute scaling to fit entire map
      val scale = map.map(m => Math.min(w.toDouble / m.pixelW, h.toDouble / m.pixelH)).getOrElse(1.0)
      val drawW = map.map(m => (m.pixelW * scale).toInt).getOrElse(w)
      val drawH = map.map(m => (m.pixelH * scale).toInt).getOrElse(h)
      val offX = (w - drawW) / 2
      val offY = (h - drawH) / 2

      // Draw terrain
      map.foreach { m =>
        g2.drawImage(m.image, offX, offY, drawW, drawH, null)
      }

      // Draw units for current frame
      currentFrameData.foreach { fd =>
        fd.units.foreach { u =>
          if (u.alive) {
            val color = if (u.friendly) new Color(0, 128, 255, 200) else new Color(220, 30, 30, 200)
            g2.setColor(color)
            val left = u.x - u.width / 2
            val top  = u.y - u.height / 2
            val sx = (offX + left * scale).toInt
            val sy = (offY + top  * scale).toInt
            val sw = Math.max(1, (u.width  * scale).toInt)
            val sh = Math.max(1, (u.height * scale).toInt)
            g2.drawRect(sx, sy, sw, sh)
          }
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val simPath = if (args != null && args.length > 0) args(0) else "simulation.json"
    val simFile = new File(simPath)
    val baseDir = simFile.getParentFile
    val mapDir = new File(baseDir, "maps")

    // State shared with UI thread
    val lastModifiedRef = new java.util.concurrent.atomic.AtomicLong(0L)
    val autoReloadRef = new java.util.concurrent.atomic.AtomicBoolean(true)
    val mapRef = new java.util.concurrent.atomic.AtomicReference[Option[MapData]](None)
    val framesRef = new java.util.concurrent.atomic.AtomicReference[Vector[FrameData]](Vector.empty)

    // Initial load
    parseSimulation(simFile).foreach { case (hash, _, frames) =>
      mapRef.set(loadMap(mapDir, hash))
      framesRef.set(frames)
      lastModifiedRef.set(simFile.lastModified())
    }

    SwingUtilities.invokeLater(new Runnable { override def run(): Unit = {
      val panel = new ViewPanel(mapRef.get(), framesRef.get())

      // Controls
      val playPause = new JButton("Pause")
      val reloadBtn = new JButton("Reload")
      val autoReload = new JCheckBox("Auto-reload", true)
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

      val controls = new JPanel()
      controls.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT))
      controls.add(playPause)
      controls.add(slider)
      controls.add(timeLabel)
      controls.add(autoReload)
      controls.add(reloadBtn)
      frame.getContentPane.add(controls, BorderLayout.SOUTH)

      frame.setSize(new Dimension(1000, 800))
      frame.setLocationByPlatform(true)
      frame.setVisible(true)

      // Timer ~24 FPS
      val playingRef = new java.util.concurrent.atomic.AtomicBoolean(true)

      def reloadIfChanged(force: Boolean = false): Unit = {
        val lm = simFile.lastModified()
        if (force || (autoReloadRef.get() && lm > lastModifiedRef.get())) {
          parseSimulation(simFile).foreach { case (hash, _, frames) =>
            mapRef.set(loadMap(mapDir, hash))
            framesRef.set(frames)
            panel.map = mapRef.get()
            panel.frames = frames
            // Update slider range and keep current index within bounds
            slider.setMaximum(Math.max(0, frames.length - 1))
            panel.frameIndex = Math.min(panel.frameIndex, Math.max(0, frames.length - 1))
            slider.setValue(panel.frameIndex)
            panel.revalidate()
            panel.repaint()
            updateTimeLabel()
          }
          lastModifiedRef.set(lm)
        }
      }

      val timer = new javax.swing.Timer(1000 / 24, (_: java.awt.event.ActionEvent) => {
        reloadIfChanged(force = false)
        if (panel.frames.nonEmpty && playingRef.get()) {
          panel.frameIndex = (panel.frameIndex + 1) % panel.frames.length
          slider.setValue(panel.frameIndex)
        }
        updateTimeLabel()
        panel.repaint()
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
