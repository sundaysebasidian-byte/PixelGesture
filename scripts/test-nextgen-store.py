"""Real SDK Test Store dialog tests on this task's synthetic emulator only."""
import runpy, time, json
from pathlib import Path
ui = runpy.run_path(str(Path(__file__).with_name('nextgen_runtime_ui.py')))
dump, click, run = ui['dump'], ui['click'], ui['run']
buy = 'Gesture Profiles — Test Store only · $1.99 (Test Store)'
def await_text(text, timeout=45):
    deadline=time.monotonic()+timeout
    while time.monotonic()<deadline:
        nodes=dump()
        if any(n.get('text') == text for n in nodes):return nodes
        time.sleep(.4)
    raise AssertionError('UI did not reach '+text)
Path('nextgen-runtime-evidence').mkdir(exist_ok=True)
checks=[]
for option,status,shot,active in [
    ('CANCEL','Purchase cancelled. No access was granted.','03-cancelled.png',False),
    ('TEST FAILED PURCHASE','Request failed. Refresh or restore to try again.','04-failed.png',False),
    ('TEST VALID PURCHASE','Profiles access verified.','05-success.png',True)]:
    click(dump(),buy)
    nodes=await_text('Test Store Purchase')
    assert any('profiles_lifetime_test' in n.get('text','') and '$1.99' in n.get('text','') for n in nodes)
    click(nodes,option)
    nodes=await_text(status)
    save=next(n for n in nodes if n.get('text')=='Save current setup')
    assert (save.get('enabled')=='true')==active
    Path('nextgen-runtime-evidence/'+shot).write_bytes(run('exec-out','screencap','-p'))
    checks.append({'official_sdk_option':option,'observed_status':status,'save_enabled':active,'passed':True})
    print(json.dumps(checks[-1]),flush=True)
Path('nextgen-runtime-evidence/billing-checks.json').write_text(json.dumps(checks,indent=2)+'\n')
