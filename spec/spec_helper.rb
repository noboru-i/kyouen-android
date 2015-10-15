require "rubygems"
require 'bundler/setup'
require 'selenium-webdriver'
require 'appium_lib'

Dir.glob("spec/steps/**/*steps.rb") { |f| load f, true }
