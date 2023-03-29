package main

import (
	"encoding/json"
	"fmt"
	"net/url"
)

type OrderClient struct {
	PUrlBuilder *PrivateUrlBuilder
}
type GetRequest struct {
	urls url.Values
}

func (p *GetRequest) Init() *GetRequest {
	p.urls = url.Values{}
	return p
}

func (p *GetRequest) InitFrom(reqParams *GetRequest) *GetRequest {
	if reqParams != nil {
		p.urls = reqParams.urls
	} else {
		p.urls = url.Values{}
	}
	return p
}

func (p *GetRequest) AddParam(property string, value string) *GetRequest {
	if property != "" && value != "" {
		p.urls.Add(property, value)
	}
	return p
}

func (p *GetRequest) BuildParams() string {
	return p.urls.Encode()
}

var AccessKey string = "1112212"
var SecretKey string = "4F65x5A2bLyMWVQj3Aqp+B4w+ivaA7n5Oi2SuYtCJ9o="
var Host string = "127.0.0.1:8080"

func (oc *OrderClient) Init(accessKey string, secretKey string, host string) *OrderClient {
	oc.PUrlBuilder = new(PrivateUrlBuilder).Init(accessKey, secretKey, host)
	return oc
}

func main() {
	client := new(OrderClient).Init(AccessKey, SecretKey, Host)
	//request请求
	var GetPath string = "/convert/test"
	request := new(GetRequest).Init().AddParam("orderId", "11234")
	client.GetOrder(GetPath, request)

	//post请求
	var PostPath string = "/convert/order2"
	/*创建集合 */
	var countryCapitalMap map[string]string
	countryCapitalMap = make(map[string]string)
	/* map插入key - value对 */
	countryCapitalMap["orderId"] = "112323"
	countryCapitalMap["orderType"] = "1"
	client.PostOrder(countryCapitalMap, PostPath)
}

func (oc *OrderClient) GetOrder(path string, request *GetRequest) {
	url := oc.PUrlBuilder.Build("GET", path, request)
	getResp, getErr := HttpGet(url)
	if getErr != nil {
		fmt.Printf("http get error: %v\n", getErr)
	}
	fmt.Printf("response to json : %s\n", getResp)
}

func (oc *OrderClient) PostOrder(body map[string]string, path string) {
	url := oc.PUrlBuilder.Build("POST", path, nil)
	content, err := json.Marshal(body)
	if err != nil {
		fmt.Printf("PlaceOrderRequest to json error: %v\n", err)
	}
	getResp, getErr := HttpPost(url, string(content))
	if getErr != nil {
		fmt.Printf("http get error: %v\n", getErr)
	}
	fmt.Printf("response to json: %s\n", getResp)

}
