# Python to exercise the Protege API to get the Classes and Slots for the new API
aClses = kb.getClses()
for aCls in aClses:
    print aCls.getName() + ": "
    aSlots = aCls.getTemplateSlots()
    for aSlot in aSlots:
        print aSlot.getName() + ": " + aSlot.getValueType().toString()
        allowedClasses = aSlot.getAllowedClses()
        for anAllowedCls in allowedClasses:
            print "Allowed Clses: " + anAllowedCls.getName()
    print "\n"
