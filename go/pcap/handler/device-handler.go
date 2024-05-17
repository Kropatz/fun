package handler

import (
	"fmt"

	"github.com/google/gopacket"
	"github.com/google/gopacket/layers"
	"github.com/google/gopacket/pcap"
)

type DeviceHandler struct {
	Device string
	Filter string
	handle *pcap.Handle
}

func (d *DeviceHandler) Listen(quit chan bool) {
	h, err := pcap.OpenLive(d.Device, 1600, true, pcap.BlockForever)
	if err != nil {
    fmt.Println("Error: ", err)
    quit <- true
	}
	err = h.SetBPFFilter(d.Filter)
	if err != nil {
    fmt.Println("Error: ", err)
    quit <- true
	}
	defer h.Close()
	d.handle = h
  fmt.Println("Listening on device: ", d.Device)
	packetSource := gopacket.NewPacketSource(h, h.LinkType())
	for packet := range packetSource.Packets() {
		fmt.Println()
		d.processPacket(packet) // Do something with a packet here.
	}
}

func (d *DeviceHandler) processPacket(packet gopacket.Packet) {
  // handle IP
	ipLayer := packet.Layer(layers.LayerTypeIPv4)
	if ipLayer != nil {
		ip, _ := ipLayer.(*layers.IPv4)
		fmt.Printf("[%s] From %s to %s\n", d.Device, ip.SrcIP, ip.DstIP)
	}
  // handle DNS
	layer := packet.Layer(layers.LayerTypeDNS)
	if layer != nil {
		dns := layer.(*layers.DNS)
		for _, dnsQuestion := range dns.Questions {
			d.question(dnsQuestion.Type, string(dnsQuestion.Name))
		}
		for _, dnsAnswer := range dns.Answers {
			if dnsAnswer.Type == layers.DNSTypeA || dnsAnswer.Type == layers.DNSTypeAAAA {
				d.answer(dnsAnswer.Type, dnsAnswer.IP.String())
			} else if dnsAnswer.Type == layers.DNSTypeCNAME {
				d.answer(dnsAnswer.Type, string(dnsAnswer.CNAME))
			} else {
				d.answer(dnsAnswer.Type, "Unknown type")
			}
		}
	}
}
func (d *DeviceHandler) question(dnsType layers.DNSType, data string) {
	fmt.Printf("[%s] DNS question [%s]: %s\n", d.Device, dnsType, data)
}

func (d *DeviceHandler)  answer(dnsType layers.DNSType, data string) {
	fmt.Printf("[%s] DNS answer [%s]: %s\n", d.Device, dnsType, data)
}
