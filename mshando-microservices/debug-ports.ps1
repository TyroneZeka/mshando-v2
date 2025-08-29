# Mshando Port Debugger - PowerShell Version
# Enhanced port management for Windows environments

param(
    [string]$Action = "interactive",
    [int]$Port = 0
)

# Define colors
$Colors = @{
    Red = "Red"
    Green = "Green"
    Yellow = "Yellow"
    Blue = "Blue"
    Cyan = "Cyan"
    Magenta = "Magenta"
}

# Define Mshando ports
$MshandoPorts = @{
    "eureka" = 8761
    "gateway" = 8080
    "user" = 8081
    "task" = 8082
    "bidding" = 8083
    "payment" = 8084
    "notification" = 8085
    "postgres" = 5432
    "frontend" = 5173
    "frontend-preview" = 4173
}

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Get-PortProcess {
    param([int]$Port)
    
    try {
        $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
        if ($connections) {
            return $connections[0].OwningProcess
        }
        return $null
    }
    catch {
        # Fallback to netstat
        $netstatOutput = netstat -ano | Select-String ":$Port.*LISTENING"
        if ($netstatOutput) {
            $fields = $netstatOutput.ToString().Split() | Where-Object { $_ -ne "" }
            if ($fields.Count -ge 5) {
                return [int]$fields[4]
            }
        }
        return $null
    }
}

function Get-ProcessDetails {
    param([int]$ProcessId)
    
    try {
        $process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
        if ($process) {
            return @{
                Name = $process.ProcessName
                Path = $process.Path
                StartTime = $process.StartTime
                WorkingSet = [math]::Round($process.WorkingSet / 1MB, 2)
                CPU = $process.CPU
            }
        }
        return $null
    }
    catch {
        return $null
    }
}

function Test-PortInUse {
    param([int]$Port)
    
    try {
        $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
        return $connection -ne $null
    }
    catch {
        # Fallback to Test-NetConnection
        try {
            $result = Test-NetConnection -ComputerName "localhost" -Port $Port -WarningAction SilentlyContinue
            return $result.TcpTestSucceeded
        }
        catch {
            return $false
        }
    }
}

function Show-PortStatus {
    param([string]$ServiceName, [int]$Port)
    
    Write-ColorOutput "`n📋 Checking $ServiceName (Port $Port)" $Colors.Blue
    Write-ColorOutput "----------------------------------------" $Colors.Blue
    
    if (Test-PortInUse -Port $Port) {
        Write-ColorOutput "⚠️  Port $Port is in use" $Colors.Yellow
        
        $processId = Get-PortProcess -Port $Port
        if ($processId) {
            Write-ColorOutput "🔍 Process ID: $processId" $Colors.Magenta
            
            $processDetails = Get-ProcessDetails -ProcessId $processId
            if ($processDetails) {
                Write-ColorOutput "📝 Process Details:" $Colors.Cyan
                Write-ColorOutput "   Name: $($processDetails.Name)" $Colors.White
                Write-ColorOutput "   Memory: $($processDetails.WorkingSet) MB" $Colors.White
                if ($processDetails.Path) {
                    Write-ColorOutput "   Path: $($processDetails.Path)" $Colors.White
                }
                if ($processDetails.StartTime) {
                    Write-ColorOutput "   Started: $($processDetails.StartTime)" $Colors.White
                }
                
                # Identify service type
                switch ($processDetails.Name) {
                    "java" { Write-ColorOutput "✅ Java Application (likely Mshando microservice)" $Colors.Green }
                    "node" { Write-ColorOutput "✅ Node.js Application" $Colors.Green }
                    "postgres" { Write-ColorOutput "✅ PostgreSQL Database" $Colors.Green }
                    default { Write-ColorOutput "❓ Unknown process type" $Colors.Red }
                }
            }
            
            Write-ColorOutput "💡 To kill: Stop-Process -Id $processId -Force" $Colors.Yellow
        }
    }
    else {
        Write-ColorOutput "✅ Port $Port is available" $Colors.Green
    }
}

function Stop-PortProcess {
    param([int]$Port)
    
    Write-ColorOutput "🔪 Attempting to kill process on port $Port..." $Colors.Yellow
    
    $processId = Get-PortProcess -Port $Port
    if ($processId) {
        try {
            $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
            if ($process) {
                Write-ColorOutput "Killing process: $($process.ProcessName) (PID: $processId)" $Colors.Yellow
                Stop-Process -Id $processId -Force
                Start-Sleep -Seconds 2
                
                # Verify
                if (Test-PortInUse -Port $Port) {
                    Write-ColorOutput "❌ Port $Port is still in use" $Colors.Red
                }
                else {
                    Write-ColorOutput "✅ Port $Port is now available" $Colors.Green
                }
            }
        }
        catch {
            Write-ColorOutput "❌ Failed to kill process: $($_.Exception.Message)" $Colors.Red
        }
    }
    else {
        Write-ColorOutput "❌ No process found on port $Port" $Colors.Red
    }
}

function Show-AllPorts {
    Write-ColorOutput "`n📊 All Mshando Ports Status" $Colors.Cyan
    Write-ColorOutput "===============================" $Colors.Cyan
    
    foreach ($service in $MshandoPorts.Keys | Sort-Object) {
        $port = $MshandoPorts[$service]
        $status = if (Test-PortInUse -Port $port) { "❌ IN USE" } else { "✅ FREE" }
        $color = if (Test-PortInUse -Port $port) { $Colors.Red } else { $Colors.Green }
        
        Write-Host ("{0,-15} ({1,4}): " -f $service, $port) -NoNewline
        Write-ColorOutput $status $color
    }
}

function Stop-AllMshandoProcesses {
    Write-ColorOutput "`n🚨 STOPPING ALL MSHANDO PROCESSES" $Colors.Red
    Write-ColorOutput "=================================" $Colors.Red
    Write-ColorOutput "⚠️  This will stop all Mshando services!" $Colors.Yellow
    
    $confirm = Read-Host "Are you sure? (y/N)"
    if ($confirm -eq "y" -or $confirm -eq "Y") {
        foreach ($service in $MshandoPorts.Keys) {
            $port = $MshandoPorts[$service]
            if ($service -ne "postgres") {  # Don't kill postgres
                if (Test-PortInUse -Port $port) {
                    Write-ColorOutput "Stopping $service on port $port..." $Colors.Yellow
                    Stop-PortProcess -Port $port
                }
            }
        }
        Write-ColorOutput "✅ All Mshando processes terminated" $Colors.Green
    }
    else {
        Write-ColorOutput "❌ Operation cancelled" $Colors.Yellow
    }
}

function Show-SystemResources {
    Write-ColorOutput "`n💻 System Resources" $Colors.Cyan
    Write-ColorOutput "===================" $Colors.Cyan
    
    # Memory usage
    $memory = Get-CimInstance -ClassName Win32_OperatingSystem
    $totalMemoryGB = [math]::Round($memory.TotalVisibleMemorySize / 1MB, 2)
    $freeMemoryGB = [math]::Round($memory.FreePhysicalMemory / 1MB, 2)
    $usedMemoryGB = $totalMemoryGB - $freeMemoryGB
    
    Write-ColorOutput "💾 Memory Usage:" $Colors.Blue
    Write-ColorOutput "   Total: $totalMemoryGB GB" $Colors.White
    Write-ColorOutput "   Used:  $usedMemoryGB GB" $Colors.White
    Write-ColorOutput "   Free:  $freeMemoryGB GB" $Colors.White
    
    # Java processes
    Write-ColorOutput "`n☕ Java Processes:" $Colors.Blue
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
    if ($javaProcesses) {
        foreach ($proc in $javaProcesses) {
            $memoryMB = [math]::Round($proc.WorkingSet / 1MB, 2)
            Write-ColorOutput "   PID $($proc.Id): $memoryMB MB" $Colors.White
        }
    }
    else {
        Write-ColorOutput "   No Java processes found" $Colors.Yellow
    }
    
    # Node processes
    Write-ColorOutput "`n🟢 Node.js Processes:" $Colors.Blue
    $nodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue
    if ($nodeProcesses) {
        foreach ($proc in $nodeProcesses) {
            $memoryMB = [math]::Round($proc.WorkingSet / 1MB, 2)
            Write-ColorOutput "   PID $($proc.Id): $memoryMB MB" $Colors.White
        }
    }
    else {
        Write-ColorOutput "   No Node.js processes found" $Colors.Yellow
    }
}

function Show-ServiceLogs {
    Write-ColorOutput "`n📋 Service Logs" $Colors.Cyan
    Write-ColorOutput "===============" $Colors.Cyan
    
    $logsDir = "logs"
    if (Test-Path $logsDir) {
        $logFiles = Get-ChildItem -Path $logsDir -Filter "*.log"
        if ($logFiles) {
            Write-ColorOutput "Available logs:" $Colors.Blue
            foreach ($logFile in $logFiles) {
                $serviceName = $logFile.BaseName
                Write-ColorOutput "- $serviceName" $Colors.White
            }
            
            $serviceChoice = Read-Host "`nEnter service name to view logs (or 'all' for overview)"
            
            if ($serviceChoice -eq "all") {
                foreach ($logFile in $logFiles) {
                    Write-ColorOutput "`n=== $($logFile.Name) (last 5 lines) ===" $Colors.Blue
                    Get-Content $logFile.FullName | Select-Object -Last 5
                }
            }
            elseif (Test-Path "$logsDir\$serviceChoice.log") {
                Write-ColorOutput "`n=== $serviceChoice.log (last 20 lines) ===" $Colors.Blue
                Get-Content "$logsDir\$serviceChoice.log" | Select-Object -Last 20
            }
            else {
                Write-ColorOutput "❌ Log file not found: $logsDir\$serviceChoice.log" $Colors.Red
            }
        }
        else {
            Write-ColorOutput "❌ No log files found in $logsDir" $Colors.Red
        }
    }
    else {
        Write-ColorOutput "❌ Logs directory not found" $Colors.Red
    }
}

function Show-Menu {
    Write-ColorOutput "`n🛠️  What would you like to do?" $Colors.Green
    Write-Host "1. Check all ports"
    Write-Host "2. Check specific service"
    Write-Host "3. Kill process on specific port"
    Write-Host "4. Kill all Mshando processes"
    Write-Host "5. Show system resources"
    Write-Host "6. Show service logs"
    Write-Host "7. Quick port scan"
    Write-Host "0. Exit"
    Write-Host ""
}

# Main execution
Write-ColorOutput "🔍 Mshando Port Debugger (PowerShell)" $Colors.Cyan
Write-ColorOutput "======================================" $Colors.Cyan

if ($Action -eq "check") {
    Show-AllPorts
}
elseif ($Action -eq "kill" -and $Port -gt 0) {
    Stop-PortProcess -Port $Port
}
elseif ($Action -eq "killall") {
    Stop-AllMshandoProcesses
}
elseif ($Action -eq "resources") {
    Show-SystemResources
}
else {
    # Interactive mode
    while ($true) {
        Show-AllPorts
        Show-Menu
        $choice = Read-Host "Choose an option"
        
        switch ($choice) {
            "1" {
                foreach ($service in $MshandoPorts.Keys | Sort-Object) {
                    Show-PortStatus -ServiceName $service -Port $MshandoPorts[$service]
                }
            }
            "2" {
                Write-ColorOutput "Available services:" $Colors.Blue
                foreach ($service in $MshandoPorts.Keys | Sort-Object) {
                    Write-Host "- $service ($($MshandoPorts[$service]))"
                }
                $serviceName = Read-Host "Enter service name"
                if ($MshandoPorts.ContainsKey($serviceName)) {
                    Show-PortStatus -ServiceName $serviceName -Port $MshandoPorts[$serviceName]
                }
                else {
                    Write-ColorOutput "❌ Unknown service: $serviceName" $Colors.Red
                }
            }
            "3" {
                $portNum = Read-Host "Enter port number"
                if ($portNum -match '^\d+$') {
                    Stop-PortProcess -Port ([int]$portNum)
                }
                else {
                    Write-ColorOutput "❌ Invalid port number" $Colors.Red
                }
            }
            "4" {
                Stop-AllMshandoProcesses
            }
            "5" {
                Show-SystemResources
            }
            "6" {
                Show-ServiceLogs
            }
            "7" {
                Write-ColorOutput "`n🔍 Quick Port Scan:" $Colors.Cyan
                netstat -ano | Select-String ":8080|:8081|:8082|:8083|:8084|:8085|:8761|:5432|:5173" | Select-String "LISTENING"
            }
            "0" {
                Write-ColorOutput "👋 Goodbye!" $Colors.Green
                exit
            }
            default {
                Write-ColorOutput "❌ Invalid option" $Colors.Red
            }
        }
        
        Read-Host "`nPress Enter to continue..."
    }
}
