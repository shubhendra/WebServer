#!/usr/bin/perl -w
$company = $ENV{'QUERY_STRING'};
print "<html>";
print "<h1>Hello! This is your weight ";
if ($company =~ /shubh/) {
my $var_rand = rand();
print 50 + 10 * $var_rand;
} else {
print "80";
}
print "</h1>";
print "</html>";