# coding: utf-8

module FirstSteps
  step 'テスト対象は :device 端末' do |device|
    @device = device
  end

  step ':target をタップする' do |target|
    element = driver.find_element(:uiautomator, "new UiSelector().text(\"#{target}\");")
    element.click
  end

  step ':expected が表示されていること' do |expected|
    element = driver.find_element(:uiautomator, "new UiSelector().text(\"#{expected}\");")

    expect(element.text).to eq(expected)
  end

  step ':target としてスクリーンショットを撮る' do |target|
    sleep 5
    appium_driver.screenshot target + '.png'
  end

  def appium_driver
    case @device
    when 'android'
      desired_caps = {
        caps: {
          platformName:  "Android",
          versionNumber: "4.2",
          deviceName:    "Nexus 5 API 22",
          app:           "app/build/outputs/apk/app-debug.apk",
          appActivity:   ".TitleActivity_"
        },
        appium_lib: {
          wait: 10
        }
      }

      @appium_driver ||= Appium::Driver.new(desired_caps)
    end
    @appium_driver
  end

  def driver
    case @device
    when 'android'
      @driver ||= appium_driver.start_driver
      @driver.manage.timeouts.implicit_wait = 10
    end
    @driver
  end

  def cleanup
    if @driver
      driver.quit
      @driver = nil
    end
  end
end

RSpec.configure do |conf|
  conf.include FirstSteps
  conf.after(:each) do
    cleanup
  end
end