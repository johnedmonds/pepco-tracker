<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!--Required Facebook SDK stuff.-->
<div id="fb-root"></div>
<script>(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) return;
  js = d.createElement(s); js.id = id;
  js.src = "//connect.facebook.net/en_US/all.js#xfbml=1";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>
<div id="social" class="bottom-section">
    <ul>
        <li><div class="fb-like" data-href="http://www.facebook.com/pages/Pepco-Tracker/161247740652517" data-send="true" data-width="450" data-show-faces="false"></div></li>
        <li><a href="https://twitter.com/PepcoTracker" class="twitter-follow-button" data-show-count="false">Follow @PepcoTracker</a><script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="//platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script></li>
        <li><a href="https://twitter.com/PepcoTrackerBot" class="twitter-follow-button" data-show-count="false">Follow @PepcoTrackerBot</a><script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="//platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script></li>
    </ul>
</div>