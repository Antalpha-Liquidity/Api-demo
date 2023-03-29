package main

import (
	"fmt"
	"net/url"
	"strconv"
	"time"
)

type PrivateUrlBuilder struct {
	host    string
	akKey   string
	akValue string
	smKey   string
	smValue string
	svKey   string
	svValue string
	tKey    string

	signer *Signer
}

func (p *PrivateUrlBuilder) Init(accessKey string, secretKey string, host string) *PrivateUrlBuilder {
	p.akKey = "AccessKeyId"
	p.akValue = accessKey
	p.smKey = "SignatureMethod"
	p.smValue = "HmacSHA256"
	p.svKey = "SignatureVersion"
	p.svValue = "1"
	p.tKey = "Timestamp"

	p.host = host
	p.signer = new(Signer).Init(secretKey)

	return p
}

func (p *PrivateUrlBuilder) Build(method string, path string, request *GetRequest) string {
	time := time.Now().UnixMilli()
	timeString := strconv.FormatInt(time, 10)
	return p.BuildWithTime(method, path, timeString, request)
}

func (p *PrivateUrlBuilder) BuildWithTime(method string, path string, time string, request *GetRequest) string {

	req := new(GetRequest).InitFrom(request)
	req.AddParam(p.akKey, p.akValue)
	req.AddParam(p.smKey, p.smValue)
	req.AddParam(p.svKey, p.svValue)
	req.AddParam(p.tKey, time)

	parameters := req.BuildParams()

	signature := p.signer.Sign(method, p.host, path, parameters)

	url := fmt.Sprintf("http://%s%s?%s&Signature=%s", p.host, path, parameters, url.QueryEscape(signature))

	return url
}
