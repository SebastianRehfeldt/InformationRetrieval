import zipfile

filename = "ipg150106.zip"
in_zip = zipfile.ZipFile(filename, "r")
in_file = in_zip.open(in_zip.namelist()[0])

base = filename[:filename.rfind(".")]
out = zipfile.ZipFile(base + ".fixed.zip", "w", zipfile.ZIP_DEFLATED)
lines = []
i = 0
appltype_utility = False
for line in in_file:
    if line.startswith("<?xml"):
        if lines and appltype_utility:
            out.writestr("{}-{}.xml".format(base, i), "".join(lines))
            i+=1
        lines = []
        appltype_utility = False
    lines.append(line)
    if 'appl-type="utility"' in line:
        appltype_utility = True
        
if lines and appltype_utility:
            out.writestr("{}-{}.xml".format(base, i), "".join(lines))
out.close()
