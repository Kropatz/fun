package main

import (
	"fmt"
	"strings"

	"github.com/Kropatz/fun/go/pcap/handler"

	"github.com/google/gopacket/pcap"
)

func cap(device string, filter string) (*pcap.Handle, error) {
	h, err := pcap.OpenLive(device, 1600, true, pcap.BlockForever)
	if err != nil {
		return nil, err
	}
	err = h.SetBPFFilter(filter)
	if err != nil {
		return nil, err
	}
	return h, nil
}

func main() {
	devices, err := getAllPhysicalDevices()
	if err != nil {
		return
	}

	quit := make(chan bool)
	for _, device := range devices {
		deviceHandler := &handler.DeviceHandler{Device: device, Filter: "udp port 53"}
		go deviceHandler.Listen(quit)
	}
	if <-quit {
		fmt.Println("Quitting...")
		return
	}
}

func getAllPhysicalDevices() ([]string, error) {
	// Find all devices
	devices, err := pcap.FindAllDevs()
	if err != nil {
		return nil, err
	}

	// Print device information
	var deviceNames []string
	fmt.Println("Devices found:")
	for _, device := range devices {
		if strings.HasPrefix(device.Name, "en") || strings.HasPrefix(device.Name, "eth") || strings.HasPrefix(device.Name, "wlan") || strings.HasPrefix(device.Name, "wg") {
			deviceNames = append(deviceNames, device.Name)
		}
		fmt.Println("\nName: ", device.Name)
		fmt.Println("Description: ", device.Description)
		fmt.Println("Devices addresses: ", device.Description)
		for _, address := range device.Addresses {
			fmt.Println("- IP address: ", address.IP)
			fmt.Println("- Subnet mask: ", address.Netmask)
		}
	}
	// return all the name of all Devices
	return deviceNames, nil
}
