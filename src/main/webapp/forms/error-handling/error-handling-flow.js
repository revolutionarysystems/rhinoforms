{
	docBase: "/myData",
	formLists: {
		main: [
				{ id: "home", url: "error-handling.html", actions: [
                        {
                            name: "next",
                            submission: {
                                url: "http://httpbin.org/status/200"
                            }
                        },{
                            name: "fail1",
                            submission: {
                                url: "http://httpbin.org/status/500",
                            }
                        },{
                            name: "fail2",
                            submission: {
                                url: "http://httpbin.org/status/500",
                                errorHandlers: [
                                    {
                                        code: 500,
                                        target: "error"
                                    }
                                ]
                            }
                        }
                ] },
                { id: "success", url: "success.html"},
                { id: "error", url: "error.html"}
			]
	}
}