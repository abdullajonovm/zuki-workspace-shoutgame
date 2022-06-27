mkdir temp

REM AMR -> PCM WAV
ffmpeg -y -i %1\%3.amr temp\%3.wav

REM PCM WAV -> MP3
lame -h --resample 22.05 temp\%3.wav %2\%3.mp3

del temp\%3.wav

REM exit 0
