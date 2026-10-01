"""Operate only the task-owned synthetic Android emulator; no SDK response fakes."""
import subprocess, sys, re, os, xml.etree.ElementTree as ET
from pathlib import Path
SERIAL = os.environ.get('NEXTGEN_TEST_SERIAL', 'emulator-5582')
if not SERIAL.startswith('emulator-'): raise RuntimeError('Only a synthetic emulator is allowed')
ADB = [os.environ.get('ADB_BIN', 'adb'), '-P', os.environ.get('NEXTGEN_ADB_PORT', '5039'), '-s', SERIAL]
def run(*args):
    return subprocess.check_output(ADB + list(args), stderr=subprocess.STDOUT)
def dump():
    run('shell', 'uiautomator', 'dump', '/sdcard/nextgen-window.xml')
    data = run('shell', 'cat', '/sdcard/nextgen-window.xml')
    Path('runtime-window.xml').write_bytes(data)
    return list(ET.fromstring(data).iter('node'))
def show(nodes):
    for n in nodes:
        if n.get('text'): print(n.get('text'), n.get('bounds'), 'enabled='+n.get('enabled',''))
def click(nodes, text):
    matches = [n for n in nodes if n.get('text') == text and n.get('enabled') == 'true']
    if text in ('Home','Gestures','Profiles'):
        matches = [n for n in matches if n.get('class') == 'android.widget.Button']
    if len(matches) != 1: raise RuntimeError('Expected one enabled target: '+text)
    x1,y1,x2,y2 = map(int,re.findall(r'\d+',matches[0].get('bounds')))
    run('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2))
if run('emu','avd','name').decode().splitlines()[0] != os.environ.get('NEXTGEN_AVD_NAME','PixelGesture_NextGen_QA'):
    raise RuntimeError('Refusing a different AVD')
nodes = dump()
action = sys.argv[1] if len(sys.argv)>1 else 'dump'
if action == 'profiles':
    click(nodes,'Profiles');nodes=dump()
elif action == 'click':
    click(nodes,sys.argv[2]);nodes=dump()
elif action == 'text':
    run('shell','input','text',sys.argv[2]);nodes=dump()
show(nodes)
