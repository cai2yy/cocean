<html>
  <head>
	  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
	  <meta charset="utf8">
	  <script src="/armot/pub/jquery-3.3.1.slim.min.js"></script>
	  <script src="/armot/pub/popper.min.js"></script>
	  <script src="/armot/pub/bootstrap.min.js"></script>
	  <script src="/armot/pub/axios.min.js"></script>
	  <link rel="stylesheet" type="text/css" href="/armot/pub/bootstrap.min.css"/>
  </head>
  <body>
    <div class="container">
      <div class="jumbotron">
      	<h1 class="display-4">Hello, ${user.name}</h1>
      	<p class="lead">You have access this page for <span id="counter">${user.counter}</span> times</p>
      	<hr class="my-4">
      </div>
    </div>
  </body>
  <script>
  $(document).ready(function() {
  	setInterval(function() {
  		axios.get("/kids/user/counter.json").then(function(data) {
  			$('#counter').html(data.data.counter);
  		});
  	}, 1000);
  });
  </script>
</html>
