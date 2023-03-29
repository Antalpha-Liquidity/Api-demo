package main

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"strings"
)

func HttpGet(url string) (string, error) {
	client := http.Client{}
	fmt.Println("http get url: ", url)
	resp, err := client.Get(url)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()
	result, err := ioutil.ReadAll(resp.Body)
	return string(result), err
}

func HttpPost(url string, body string) (string, error) {
	client := http.Client{}
	fmt.Println("http post url:", url)
	resp, err := client.Post(url, "application/json", strings.NewReader(body))
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()
	result, err := ioutil.ReadAll(resp.Body)
	return string(result), err
}
