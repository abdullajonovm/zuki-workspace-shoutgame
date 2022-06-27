mkdir temp

REM FLV -> WAV
testCrispa "%1\%~3.flv" "temp\%~3.wav" flv2wav
IF %ERRORLEVEL% == 0 GOTO WAV2MP3
EXIT 1

:WAV2MP3
REM PCM WAV -> MP3
lame -h --resample 22.05 "temp\%~3.wav" "%2\%~3.mp3"
IF %ERRORLEVEL% == 0 GOTO END
EXIT 1

:END
