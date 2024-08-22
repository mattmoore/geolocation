object WelcomeScreen {
  import embroidery.*
  import scala.Console

  lazy val pixelsWithArt: List[PixelAsciiArt] = List(
    PixelAsciiArt(Pixel(255), Art(' ')),
    PixelAsciiArt(Pixel(230), Art('.')),
    PixelAsciiArt(Pixel(220), Art(',')),
    PixelAsciiArt(Pixel(210), Art('°')),
    PixelAsciiArt(Pixel(200), Art('²')),
    PixelAsciiArt(Pixel(190), Art('*')),
    PixelAsciiArt(Pixel(180), Art('^')),
    PixelAsciiArt(Pixel(170), Art('~')),
    PixelAsciiArt(Pixel(150), Art('/')),
    PixelAsciiArt(Pixel(140), Art('|')),
    PixelAsciiArt(Pixel(130), Art('s')),
    PixelAsciiArt(Pixel(120), Art('q')),
    PixelAsciiArt(Pixel(90), Art('µ')),
    PixelAsciiArt(Pixel(70), Art('@')),
    PixelAsciiArt(Pixel(0), Art('#')),
  )

  lazy val scalaLogo = logo.asciiArt("project/images/satellite.jpg", pixelsWithArt, 40, 40)

  def projectInformation(version: String, scalaVersion: String) =
    s"""|
        |${Console.RED}Scala${Console.RESET} version: $scalaVersion
        |Project version: $version
        |""".stripMargin

  def apply(version: String, scalaVersion: String): String =
    s"""|$scalaLogo
        |${projectInformation(version, scalaVersion)}
        |""".stripMargin
}
