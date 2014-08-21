{
	docBase: "/myData",
	libraries: ["js/testUtil.js"],
	formLists: {
		main: [
				{ id: "one", url: "one.html",
                    actions: [ 
                        "next",
                        {
                            name: "send-bad-submission",
                            submission: {
                                url: "http://dummyUrl",
                                errorHandlers:[
                                    {
                                        code: 500,
                                        target: "error"
                                    }
                                ]
                            }
                        }
                    ] },
				{ id: "two", url: "two.html", actions: [ "back", "add:anotherList.editFish(fishIndex=next)", "edit:anotherList.editFish(fishIndex=?)", "indexTest:indexTestList.indexTestA", "next" ] },
				{ id: "three", url: "three.html",
					actions: [
						"back",
						"finish",
						{
							name: "send-to-my-server",
							submission: {
								preTransform: "xslt/toServerFormat.xsl",
								url: "{{$dummy-submission-url}}",
								method: "post",
								data: {
									type: "10",
									paramA: "[dataDocument]"
								},
								messageOnHttpError: "{{$message.dummy.submission}}",
								postTransform: "xslt/fromServerFormat.xsl",
								resultInsertPoint: "/myData/submissionResult",
								jsonToXml: false,
								jsonToXmlTypeHints: false,
								jsonToXmlRootName: "JsonResponse"
							},
							submissions: [
								{
									url: "{{$static-xml-in-webapp-url}}",
								},
								{
									url: "{{$dummy-submission-url}}",
								}
							],
							clearTargetFormDocBase: "true"
						},
						{
							/** Named cancel action with specific target */
							name: "cancel-back-to-one",
							type: "cancel",
							target: "one"
						}
					]
				},
                {id: "error", url: "error.html"}
		],
		anotherList: [
			{ id: "editFish", docBase: "fishes/fish[fishIndex]", url: "editFish.html",
				actions: [
				          "cancel",
				          "addGill:gills.editGill(gillIndex=next)",
				          {
				        	  name: "transform",
				        	  dataDocTransform: "something.xsl"  
				          },
				          "next" 
				          ] 
			}
		],
		gills: [
			{ id: "editGill", docBase: "gills/gill[gillIndex]", url: "editGill.html", actions: [ "cancel", "next" ] }
		],
		indexTestList: [
			{ id: "indexTestA", docBase: "fishes", url: "editFish.html", actions: [ "next(fishIndex=next)" ] },
			{ id: "indexTestB", docBase: "fishes", url: "editFish.html" }
		]
	}
}
