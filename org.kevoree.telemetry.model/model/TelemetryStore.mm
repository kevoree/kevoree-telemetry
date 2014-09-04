class org.kevoree.telemetry.store.TelemetryStore {
    @contained
    nodes : org.kevoree.telemetry.store.Node[0,*]
}

class org.kevoree.telemetry.store.Node {
    @id
    name : String

    @contained
    log : org.kevoree.telemetry.store.LogTicket

    @contained
    memory : org.kevoree.telemetry.store.MemoryInfoTicket

    @contained
    runtime : org.kevoree.telemetry.store.RuntimeInfoTicket

    @contained
    os : org.kevoree.telemetry.store.OperatingSystemInfoTicket

    @contained
    threads : org.kevoree.telemetry.store.ThreadInfoTicket
}

class org.kevoree.telemetry.store.Ticket {
    origin:String
    type:String
}

class org.kevoree.telemetry.store.LogTicket : org.kevoree.telemetry.store.Ticket {
    message:String
    stack:String
}

class org.kevoree.telemetry.store.MemoryInfoTicket : org.kevoree.telemetry.store.Ticket {
    pendingFinalization : Int
    @contained
    heapMemory : org.kevoree.telemetry.store.MemoryInfo
    @contained
    offHeapMemory : org.kevoree.telemetry.store.MemoryInfo
}

class org.kevoree.telemetry.store.MemoryInfo {
    init : Long
    committed : Long
    max : Long
    used : Long
}

class org.kevoree.telemetry.store.RuntimeInfoTicket : org.kevoree.telemetry.store.Ticket {
    name : String
    @contained
    vm : org.kevoree.telemetry.store.VmDetail
    @contained
    spec : org.kevoree.telemetry.store.VmDetail
    bootClasspath : String
    classPath : String
    libraryPath : String
    managementSpecVersion : String
    startTime : Long
    upTime : Long
    @contained
    inputArguments : String[0,*]
    @contained
    systemProperties : org.kevoree.telemetry.store.KeyValuePair[0,*]
}

class org.kevoree.telemetry.store.VmDetail {
    vendor : String
    name : String
    version : String
}


class org.kevoree.telemetry.store.OperatingSystemInfoTicket : org.kevoree.telemetry.store.Ticket {
    name : String
    architecture : String
    availableProcessors : Int
    loadAverage : Float
    version : String
}

class org.kevoree.telemetry.store.ThreadInfoTicket : org.kevoree.telemetry.store.Ticket {

    cpuTime : Long
    userTime : Long
    daemonThreadCount : Int
    peakThreadCount : Int
    threadCount : Int
    totalStartedThreadCount : Int

    @contained
    allTherads : org.kevoree.telemetry.store.ThreadInfo[0,*]
    @contained
    deadlockTherads : org.kevoree.telemetry.store.ThreadInfo[0,*]

}

class  org.kevoree.telemetry.store.ThreadInfo {
    @id
    threadId : String
    threadName : String
    threadState : String
    blockedCount : Int
    blockedTime : Long
    waitedCount : Int
    waitedTime : Long
    @contained
    lockInfo : org.kevoree.telemetry.store.ThreadLockInfo
}

class  org.kevoree.telemetry.store.ThreadLockInfo {
    name : String
    ownerId : String
    ownerName : String
}

class  org.kevoree.telemetry.store.KeyValuePair {
    key : String
    value : String
}