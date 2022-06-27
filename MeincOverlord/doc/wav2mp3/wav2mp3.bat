mkdir temp

REM IMA ADPCM WAV -> PCM WAV
sox "%1\%~3.wav" -s -t .wav "temp\%~3.wav"

REM PCM WAV -> MP3
lame -h --resample 22.05 "temp\%~3.wav" "%2\%~3.mp3"

del temp\%3.wav

REM exit 0
