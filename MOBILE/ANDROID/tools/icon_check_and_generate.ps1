param(
  [string]$BackgroundPath = "D:\Git\Synapse\MOBILE\ANDROID\IMG\ic_launcher_background.png",
  [string]$ForegroundPath = "D:\Git\Synapse\MOBILE\ANDROID\IMG\ic_launcher_foreground.png"
)

$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

$bgPath = $BackgroundPath
$fgPath = $ForegroundPath
$resRoot = "D:\Git\Synapse\MOBILE\ANDROID\app\src\main\res"

function New-Bitmap([int]$w, [int]$h) {
  return New-Object System.Drawing.Bitmap $w, $h, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
}

function Set-HighQuality([System.Drawing.Graphics]$g) {
  $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
  $g.InterpolationMode  = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
  $g.SmoothingMode      = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
  $g.PixelOffsetMode    = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
}

function Test-Alpha([System.Drawing.Bitmap]$bmp) {
  $w = $bmp.Width
  $h = $bmp.Height
  $stepX = [Math]::Max(1, [int]($w / 64))
  $stepY = [Math]::Max(1, [int]($h / 64))
  $samples = 0
  $nonOpaque = 0
  for ($y = 0; $y -lt $h; $y += $stepY) {
    for ($x = 0; $x -lt $w; $x += $stepX) {
      $samples++
      if ($bmp.GetPixel($x, $y).A -lt 255) { $nonOpaque++ }
    }
  }
  return @{ samples = $samples; nonOpaque = $nonOpaque }
}

$bg = [System.Drawing.Bitmap]::FromFile($bgPath)
$fgInput = [System.Drawing.Bitmap]::FromFile($fgPath)

if ($bg.Width -ne 1024 -or $bg.Height -ne 1024) { throw "Background must be 1024x1024, got $($bg.Width)x$($bg.Height)" }

# Foreground can be either 1024x1024 (full canvas) or 672x672 (safe-zone sized).
$fg = $null
if ($fgInput.Width -eq 1024 -and $fgInput.Height -eq 1024) {
  $fg = $fgInput
} elseif ($fgInput.Width -eq 672 -and $fgInput.Height -eq 672) {
  $fg = New-Bitmap 1024 1024
  $g = [System.Drawing.Graphics]::FromImage($fg)
  Set-HighQuality $g
  $g.Clear([System.Drawing.Color]::Transparent)
  $dst = New-Object System.Drawing.Rectangle 176, 176, 672, 672
  $g.DrawImage($fgInput, $dst)
  $g.Dispose()
  $fgInput.Dispose()
} else {
  $w = $fgInput.Width
  $h = $fgInput.Height
  $fgInput.Dispose()
  throw "Foreground must be 1024x1024 or 672x672, got ${w}x${h}"
}

$alpha = Test-Alpha $fg
Write-Host ("FG alpha sample: nonOpaque=" + $alpha.nonOpaque + "/" + $alpha.samples)

$dens = @(
  @{ name = "mipmap-mdpi";  size = 108 },
  @{ name = "mipmap-hdpi";  size = 162 },
  @{ name = "mipmap-xhdpi"; size = 216 },
  @{ name = "mipmap-xxhdpi"; size = 324 },
  @{ name = "mipmap-xxxhdpi"; size = 432 }
)

foreach ($d in $dens) {
  $dir = Join-Path $resRoot $d.name
  New-Item -ItemType Directory -Force -Path $dir | Out-Null

  $S = [int]$d.size

  $bgS = New-Bitmap $S $S
  $g = [System.Drawing.Graphics]::FromImage($bgS)
  Set-HighQuality $g
  $g.DrawImage($bg, 0, 0, $S, $S)
  $g.Dispose()

  $fgS = New-Bitmap $S $S
  $g = [System.Drawing.Graphics]::FromImage($fgS)
  Set-HighQuality $g
  $g.Clear([System.Drawing.Color]::Transparent)
  $g.DrawImage($fg, 0, 0, $S, $S)
  $g.Dispose()

  $bgS.Save((Join-Path $dir "ic_launcher_background.png"), [System.Drawing.Imaging.ImageFormat]::Png)
  $fgS.Save((Join-Path $dir "ic_launcher_foreground.png"), [System.Drawing.Imaging.ImageFormat]::Png)

  $legacy = New-Bitmap $S $S
  $g = [System.Drawing.Graphics]::FromImage($legacy)
  Set-HighQuality $g
  $g.DrawImage($bgS, 0, 0, $S, $S)
  $g.DrawImage($fgS, 0, 0, $S, $S)
  $g.Dispose()

  $legacy.Save((Join-Path $dir "ic_launcher.png"), [System.Drawing.Imaging.ImageFormat]::Png)
  $legacy.Save((Join-Path $dir "ic_launcher_round.png"), [System.Drawing.Imaging.ImageFormat]::Png)

  $bgS.Dispose()
  $fgS.Dispose()
  $legacy.Dispose()
}

$bg.Dispose()
$fg.Dispose()

Write-Host "OK"

